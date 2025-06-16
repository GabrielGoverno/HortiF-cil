package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;
import com.hortifacil.model.Produto;
import com.hortifacil.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

  public int salvarPedido(Pedido pedido) {
    String sql = "INSERT INTO pedido (id_cliente, data_pedido, total, status, ativo) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setInt(1, pedido.getIdCliente());
        stmt.setDate(2, Date.valueOf(pedido.getDataPedido())); // usa LocalDate
        stmt.setDouble(3, pedido.getTotal());
        stmt.setString(4, pedido.getStatus());
        stmt.setBoolean(5, pedido.isAtivo());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1); // ID gerado
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}

    public List<Pedido> listarPedidosPorCliente(int idCliente) {
    List<Pedido> pedidos = new ArrayList<>();

    String sql = "SELECT id_pedido, data_pedido, total, status, ativo " +
                 "FROM pedido WHERE id_cliente = ? ORDER BY data_pedido DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Pedido pedido = new Pedido();
            pedido.setIdPedido(rs.getInt("id_pedido"));
            pedido.setIdCliente(idCliente);
            pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
            pedido.setTotal(rs.getDouble("total"));
            pedido.setStatus(rs.getString("status"));
            pedido.setAtivo(rs.getBoolean("ativo"));

            pedidos.add(pedido);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return pedidos;
}
public void salvarItensPedido(int idPedido, List<CarrinhoProduto> itensCarrinho) {
    String sql = "INSERT INTO pedido_produto (id_pedido, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        for (CarrinhoProduto item : itensCarrinho) {
            stmt.setInt(1, idPedido);
            stmt.setInt(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setDouble(4, item.getPrecoUnitario());
            stmt.addBatch();
        }

        stmt.executeBatch();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public int salvarPedido(Pedido pedido, Connection conn) throws SQLException {
    String sql = "INSERT INTO pedido (id_cliente, data_pedido, total, status, ativo) VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, pedido.getIdCliente());
        stmt.setDate(2, Date.valueOf(pedido.getDataPedido()));
        stmt.setDouble(3, pedido.getTotal());
        stmt.setString(4, pedido.getStatus());
        stmt.setBoolean(5, pedido.isAtivo());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
    }
    return -1;
}

public void salvarItensPedido(int idPedido, List<CarrinhoProduto> itensCarrinho, Connection conn) throws SQLException {
    String sql = "INSERT INTO pedido_produto (id_pedido, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (CarrinhoProduto item : itensCarrinho) {
            stmt.setInt(1, idPedido);
            stmt.setInt(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setDouble(4, item.getPrecoUnitario());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }
}

public boolean atualizarStatus(int idPedido, String status) {
    String sql = "UPDATE pedido SET status = ? WHERE id_pedido = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, status);
        stmt.setInt(2, idPedido);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public List<Pedido> listarTodosPedidos() {
    List<Pedido> pedidos = new ArrayList<>();
    String sql = "SELECT * FROM pedido ORDER BY data_pedido DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Pedido pedido = new Pedido();
            pedido.setIdPedido(rs.getInt("id_pedido"));
            pedido.setIdCliente(rs.getInt("id_cliente"));
            pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
            pedido.setTotal(rs.getDouble("total"));
            pedido.setStatus(rs.getString("status"));
            pedido.setAtivo(rs.getBoolean("ativo"));

            // Para cada pedido, busque os itens
            List<CarrinhoProduto> itens = listarItensPorPedido(pedido.getIdPedido());
            pedido.setItens(itens);

            pedidos.add(pedido);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return pedidos;
}

public List<CarrinhoProduto> listarItensPorPedido(int idPedido) {
    List<CarrinhoProduto> itens = new ArrayList<>();
    String sql = "SELECT p.id_produto, p.nome, pp.quantidade, pp.preco_unitario FROM pedido_produto pp " +
                 "JOIN produto p ON pp.id_produto = p.id_produto " +
                 "WHERE pp.id_pedido = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idPedido);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Produto produto = new Produto(
                rs.getInt("id_produto"),
                rs.getString("nome"),
                rs.getDouble("preco_unitario"),
                "", "", null // ajuste conforme construtor
            );
            CarrinhoProduto item = new CarrinhoProduto();
            item.setProduto(produto);
            item.setQuantidade(rs.getInt("quantidade"));
            item.setPrecoUnitario(rs.getDouble("preco_unitario"));
            itens.add(item);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return itens;
}

}
