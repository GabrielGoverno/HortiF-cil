package com.hortifacil.service;

import com.hortifacil.dao.PedidoDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;
import com.hortifacil.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoService {

    private static PedidoService instance;
    private PedidoDAO pedidoDAO;

    private PedidoService() {
        pedidoDAO = new PedidoDAO();
    }

    public static PedidoService getInstance() {
        if (instance == null) {
            instance = new PedidoService();
        }
        return instance;
    }

    public boolean criarPedido(int clienteId, List<CarrinhoProduto> itensCarrinho) {
        double total = 0.0;

        for (CarrinhoProduto item : itensCarrinho) {
            total += item.getQuantidade() * item.getPrecoUnitario();
        }

        Pedido pedido = new Pedido(
            clienteId,
            LocalDate.now(),
            total,
            "EM_ANDAMENTO", // status inicial
            true            // ativo
        );

        int idPedido = pedidoDAO.salvarPedido(pedido);
        if (idPedido == -1) {
            return false;
        }

        pedidoDAO.salvarItensPedido(idPedido, itensCarrinho);
        return true;
    }

    public List<Pedido> listarPedidosPorCliente(int clienteId) {
        return pedidoDAO.listarPedidosPorCliente(clienteId);
    }

    public boolean finalizarPedido(int clienteId) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // inicia transação

        // 1. Obter carrinho aberto do cliente
        String sqlCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        int idCarrinho;
        try (PreparedStatement psCarrinho = conn.prepareStatement(sqlCarrinho)) {
            psCarrinho.setInt(1, clienteId);
            ResultSet rs = psCarrinho.executeQuery();
            if (rs.next()) {
                idCarrinho = rs.getInt("id_carrinho");
            } else {
                System.out.println("Nenhum carrinho aberto encontrado para o cliente.");
                conn.rollback();
                return false;
            }
        }

        // 2. Obter itens do carrinho
        String sqlItens = "SELECT cp.id_produto, cp.quantidade, cp.preco_unitario, p.nome " +
                          "FROM carrinho_produto cp JOIN produto p ON cp.id_produto = p.id " +
                          "WHERE cp.id_carrinho = ?";
        List<CarrinhoProduto> itens = new ArrayList<>();

        try (PreparedStatement psItens = conn.prepareStatement(sqlItens)) {
            psItens.setInt(1, idCarrinho);
            ResultSet rs = psItens.executeQuery();

            while (rs.next()) {
                int idProduto = rs.getInt("id_produto");
                int quantidade = rs.getInt("quantidade");
                double precoUnitario = rs.getDouble("preco_unitario");
                String nome = rs.getString("nome");

                // Crie Produto e CarrinhoProduto conforme sua implementação
                Produto produto = new Produto(idProduto, nome, precoUnitario, null, null, null); // ajuste conforme seu construtor
                CarrinhoProduto cp = new CarrinhoProduto();
                cp.setProduto(produto);
                cp.setQuantidade(quantidade);
                cp.setPrecoUnitario(precoUnitario);
                itens.add(cp);
            }
        }

        if (itens.isEmpty()) {
            System.out.println("Carrinho está vazio.");
            conn.rollback();
            return false;
        }

        // 3. Verificar estoque e deduzir estoque
        String sqlEstoqueSelect = "SELECT quantidade FROM estoque WHERE id_produto = ? FOR UPDATE";
        String sqlEstoqueUpdate = "UPDATE estoque SET quantidade = quantidade - ? WHERE id_produto = ?";

        try (PreparedStatement psEstoqueSelect = conn.prepareStatement(sqlEstoqueSelect);
             PreparedStatement psEstoqueUpdate = conn.prepareStatement(sqlEstoqueUpdate)) {

            for (CarrinhoProduto item : itens) {
                int idProduto = item.getProduto().getId();
                int qtdPedido = item.getQuantidade();

                psEstoqueSelect.setInt(1, idProduto);
                ResultSet rsEstoque = psEstoqueSelect.executeQuery();

                if (rsEstoque.next()) {
                    int qtdEstoque = rsEstoque.getInt("quantidade");
                    if (qtdEstoque < qtdPedido) {
                        System.out.println("Estoque insuficiente para o produto: " + item.getProduto().getNome());
                        conn.rollback();
                        return false;
                    }
                } else {
                    System.out.println("Produto não encontrado no estoque: " + item.getProduto().getNome());
                    conn.rollback();
                    return false;
                }

                // Deduzir estoque
                psEstoqueUpdate.setInt(1, qtdPedido);
                psEstoqueUpdate.setInt(2, idProduto);
                psEstoqueUpdate.executeUpdate();
            }
        }

        // 4. Atualizar status do carrinho para FECHADO
        String sqlAtualizaCarrinho = "UPDATE carrinho SET status = 'FECHADO', data_fechamento = CURRENT_DATE WHERE id_carrinho = ?";
        try (PreparedStatement psAtualiza = conn.prepareStatement(sqlAtualizaCarrinho)) {
            psAtualiza.setInt(1, idCarrinho);
            psAtualiza.executeUpdate();
        }

        // 5. Criar pedido e salvar itens usando PedidoDAO (adaptar para usar a mesma conexão)
        double total = itens.stream().mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario()).sum();
        Pedido pedido = new Pedido(clienteId, LocalDate.now(), total, "FINALIZADO", true);
        PedidoDAO pedidoDAO = new PedidoDAO();

        // Aqui, adapte os métodos do PedidoDAO para aceitar conexão externa para transação
        int idPedido = pedidoDAO.salvarPedido(pedido, conn);
        if (idPedido == -1) {
            System.out.println("Falha ao salvar pedido.");
            conn.rollback();
            return false;
        }
        pedidoDAO.salvarItensPedido(idPedido, itens, conn);

        conn.commit();
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

public boolean atualizarStatusPedido(Pedido pedido) {
    return pedidoDAO.atualizarStatus(pedido.getIdPedido(), pedido.getStatus());
}

public List<Pedido> listarTodosPedidos() {
    return pedidoDAO.listarTodosPedidos();
}


}
