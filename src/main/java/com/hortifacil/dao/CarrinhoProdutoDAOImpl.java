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
    
    
    public int obterClienteIdPorUsuarioId(int usuarioId) throws SQLException {
    String sql = "SELECT id FROM cliente WHERE id_usuario = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Cliente não encontrado para usuário: " + usuarioId);
        }
    }
}

public boolean clienteExiste(int clienteId) throws SQLException {
    String sql = "SELECT 1 FROM cliente WHERE id_cliente = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }
}

    @Override
    public void adicionarAoCarrinho(CarrinhoProduto item) throws SQLException {
            System.out.println("Cliente ID no item: " + item.getClienteId());  
        int idCarrinho = obterOuCriarCarrinhoAberto(item.getClienteId());

        String verificaProduto = "SELECT quantidade FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(verificaProduto)) {
            stmt.setInt(1, idCarrinho);
            stmt.setInt(2, item.getProduto().getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int quantidadeAtual = rs.getInt("quantidade");
                int novaQuantidade = quantidadeAtual + item.getQuantidade();

                String atualizaQuantidade = "UPDATE carrinho_produto SET quantidade = ? WHERE id_carrinho = ? AND id_produto = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(atualizaQuantidade)) {
                    updateStmt.setInt(1, novaQuantidade);
                    updateStmt.setInt(2, idCarrinho);
                    updateStmt.setInt(3, item.getProduto().getId());
                    updateStmt.executeUpdate();
                }
            } else {
                String insereProduto = "INSERT INTO carrinho_produto (id_carrinho, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insereProduto)) {
                    insertStmt.setInt(1, idCarrinho);
                    insertStmt.setInt(2, item.getProduto().getId());
                    insertStmt.setInt(3, item.getQuantidade());
                    insertStmt.setDouble(4, item.getPrecoUnitario());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public int obterOuCriarCarrinhoAberto(int clienteId) throws SQLException {
        if (!clienteExiste(clienteId)) {
            throw new SQLException("Cliente não encontrado: " + clienteId);
        }

        String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        try (PreparedStatement stmt = connection.prepareStatement(buscaCarrinho)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_carrinho");
            }
        }

        String insereCarrinho = "INSERT INTO carrinho (id_cliente, status) VALUES (?, 'ABERTO')";
        try (PreparedStatement stmtInsere = connection.prepareStatement(insereCarrinho, Statement.RETURN_GENERATED_KEYS)) {
            stmtInsere.setInt(1, clienteId);
            int affectedRows = stmtInsere.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar novo carrinho para cliente: " + clienteId);
            }
            ResultSet generatedKeys = stmtInsere.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Falha ao obter ID do novo carrinho.");
            }
        }
    }

    @Override
    public boolean limparCarrinhoDoCliente(int clienteId) {
        try {
            String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
            int idCarrinho = 0;
            try (PreparedStatement stmtBusca = connection.prepareStatement(buscaCarrinho)) {
                stmtBusca.setInt(1, clienteId);
                ResultSet rs = stmtBusca.executeQuery();
                if (rs.next()) {
                    idCarrinho = rs.getInt("id_carrinho");
                } else {
                    throw new SQLException("Carrinho aberto não encontrado para cliente: " + clienteId);
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
             "WHERE c.id_cliente = ? AND c.status = 'ABERTO'";

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

    public int obterCarrinhoAbertoPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_carrinho");
            } else {
                throw new SQLException("Carrinho aberto não encontrado para cliente: " + clienteId);
            }
        }
    }

 @Override
public List<CarrinhoProduto> listarPorPedido(int pedidoId) throws SQLException {
    String sql = "SELECT pi.quantidade, pi.preco_unitario, p.id_produto, p.nome, p.id_unidade, u.nome as unidade_nome " +
                 "FROM pedido_item pi " +
                 "JOIN produto p ON pi.id_produto = p.id_produto " +
                 "JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE pi.id_pedido = ?";

    List<CarrinhoProduto> itens = new ArrayList<>();

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, pedidoId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id_produto"));
                produto.setNome(rs.getString("nome"));
                // Não setar preco aqui, pois não existe p.preco no select
                produto.setUnidade(new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("unidade_nome")));

                CarrinhoProduto item = new CarrinhoProduto();
                item.setProduto(produto);
                item.setQuantidade(rs.getInt("quantidade"));
                item.setPrecoUnitario(rs.getDouble("preco_unitario"));

                itens.add(item);
            }
        }
    }

    return itens;
}

   @Override
public boolean removerItem(int clienteId, int produtoId) {
    try {
        int idCarrinho = obterCarrinhoAbertoPorCliente(clienteId);
        String sql = "DELETE FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCarrinho);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


}