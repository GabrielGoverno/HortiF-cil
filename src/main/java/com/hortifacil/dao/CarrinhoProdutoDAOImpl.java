package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoProdutoDAOImpl implements CarrinhoProdutoDAO {

    private final Connection connection;

    public CarrinhoProdutoDAOImpl(Connection connection) {
        this.connection = connection;
    }

   @Override
public boolean adicionarAoCarrinho(CarrinhoProduto item) {
    try {
        // Buscar id_carrinho pelo cliente_id
        String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        int idCarrinho;

        try (PreparedStatement stmtBusca = connection.prepareStatement(buscaCarrinho)) {
            stmtBusca.setInt(1, item.getClienteId());
            ResultSet rs = stmtBusca.executeQuery();
            if (rs.next()) {
                idCarrinho = rs.getInt("id_carrinho");
            } else {
                throw new SQLException("Carrinho não encontrado para cliente: " + item.getClienteId());
            }
        }

        // Buscar estoque disponível do produto
        String estoqueSql = "SELECT quantidade FROM estoque WHERE id_produto = ?";
        int estoqueDisponivel = 0;
        try (PreparedStatement stmtEstoque = connection.prepareStatement(estoqueSql)) {
            stmtEstoque.setInt(1, item.getProduto().getId());
            ResultSet rsEstoque = stmtEstoque.executeQuery();
            if (rsEstoque.next()) {
                estoqueDisponivel = rsEstoque.getInt("quantidade");
            } else {
                throw new SQLException("Produto não encontrado no estoque: " + item.getProduto().getId());
            }
        }

        // Verifica se o produto já está no carrinho
        String verificaSql = "SELECT quantidade FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ?";
        try (PreparedStatement stmtVerifica = connection.prepareStatement(verificaSql)) {
            stmtVerifica.setInt(1, idCarrinho);
            stmtVerifica.setInt(2, item.getProduto().getId());
            ResultSet rs = stmtVerifica.executeQuery();

            int quantidadeAtual = 0;
            if (rs.next()) {
                quantidadeAtual = rs.getInt("quantidade");
            }

            int novaQuantidade = quantidadeAtual + item.getQuantidade();

            // Verifica se novaQuantidade ultrapassa estoque
            if (novaQuantidade > estoqueDisponivel) {
                System.out.println("Quantidade solicitada ultrapassa o estoque disponível.");
                return false;
            }

            if (quantidadeAtual > 0) {
                // Atualiza quantidade
                String updateSql = "UPDATE carrinho_produto SET quantidade = ? WHERE id_carrinho = ? AND id_produto = ?";
                try (PreparedStatement stmtUpdate = connection.prepareStatement(updateSql)) {
                    stmtUpdate.setInt(1, novaQuantidade);
                    stmtUpdate.setInt(2, idCarrinho);
                    stmtUpdate.setInt(3, item.getProduto().getId());
                    return stmtUpdate.executeUpdate() > 0;
                }
            } else {
                // Insere novo item no carrinho
                String insertSql = "INSERT INTO carrinho_produto (id_carrinho, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmtInsert = connection.prepareStatement(insertSql)) {
                    stmtInsert.setInt(1, idCarrinho);
                    stmtInsert.setInt(2, item.getProduto().getId());
                    stmtInsert.setInt(3, item.getQuantidade());
                    stmtInsert.setDouble(4, item.getPrecoUnitario());
                    return stmtInsert.executeUpdate() > 0;
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

private int obterOuCriarCarrinhoAberto(int clienteId) throws SQLException {
    String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
    try (PreparedStatement stmt = connection.prepareStatement(buscaCarrinho)) {
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_carrinho");
        }
    }

    // Se não encontrou, cria um novo carrinho aberto
    String criaCarrinho = "INSERT INTO carrinho (id_cliente, data_criacao, status) VALUES (?, CURRENT_DATE, 'ABERTO')";
    try (PreparedStatement stmt = connection.prepareStatement(criaCarrinho, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, clienteId);
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Falha ao criar carrinho para cliente: " + clienteId);
        }
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Falha ao obter ID do carrinho criado.");
            }
        }
    }
}

@Override
public boolean limparCarrinhoDoCliente(int clienteId) {
    try {
        String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ?";
        int idCarrinho = 0;
        try (PreparedStatement stmtBusca = connection.prepareStatement(buscaCarrinho)) {
            stmtBusca.setInt(1, clienteId);
            ResultSet rs = stmtBusca.executeQuery();
            if (rs.next()) {
                idCarrinho = rs.getInt("id_carrinho");
            } else {
                throw new SQLException("Carrinho não encontrado para cliente: " + clienteId);
            }
        }

        String sql = "DELETE FROM carrinho_produto WHERE id_carrinho = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCarrinho);
            return stmt.executeUpdate() > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public List<CarrinhoProduto> listarPorCliente(int clienteId) {
    List<CarrinhoProduto> itens = new ArrayList<>();
    String sql = "SELECT cp.id_carrinho, cp.id_produto, cp.quantidade, cp.preco_unitario, " +
                 "p.nome, p.preco_unitario AS produto_preco, p.imagem_path, p.descricao, " +
                 "u.id_unidade, u.nome AS nome_unidade " +
                 "FROM carrinho_produto cp " +
                 "JOIN carrinho c ON cp.id_carrinho = c.id_carrinho " +
                 "JOIN produto p ON cp.id_produto = p.id_produto " +
                 "JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE c.id_cliente = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            UnidadeMedida unidade = new UnidadeMedida(
                rs.getInt("id_unidade"),
                rs.getString("nome_unidade")
            );

            Produto produto = new Produto(
                rs.getInt("id_produto"),
                rs.getString("nome"),
                rs.getDouble("produto_preco"),
                rs.getString("imagem_path"),
                rs.getString("descricao"),
                unidade
            );

            CarrinhoProduto item = new CarrinhoProduto(
                rs.getInt("id_carrinho"),
                clienteId,
                produto,
                rs.getInt("quantidade"),
                rs.getDouble("preco_unitario")
            );

            itens.add(item);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return itens;
}


    @Override
    public void remover(int id) {
        throw new UnsupportedOperationException("Use removerItem(clienteId, produtoId) em vez deste método.");
    }


    @Override
public boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade) {
    try {
        int idCarrinho = obterCarrinhoAbertoPorCliente(clienteId);

        String sql = "UPDATE carrinho_produto SET quantidade = ? WHERE id_carrinho = ? AND id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setInt(2, idCarrinho);
            stmt.setInt(3, produtoId);
            return stmt.executeUpdate() > 0;
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

   @Override
public void removerItem(int clienteId, int produtoId) {
    try {
        int idCarrinho = obterCarrinhoAbertoPorCliente(clienteId);

        String sql = "DELETE FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCarrinho);
            stmt.setInt(2, produtoId);
            stmt.executeUpdate();
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public int obterCarrinhoAbertoPorCliente(int clienteId) throws SQLException {
    String sql = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_carrinho");
        } else {
            throw new SQLException("Carrinho não encontrado para cliente: " + clienteId);
        }
    }
}

}
