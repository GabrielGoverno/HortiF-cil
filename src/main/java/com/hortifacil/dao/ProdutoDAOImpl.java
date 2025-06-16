package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAOImpl implements ProdutoDAO {

    private final Connection connection;

    public ProdutoDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
public int salvar(Produto produto) {
    String sql = "INSERT INTO produto (nome, preco_unitario, imagem_path, descricao, id_unidade) VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, produto.getNome());
        stmt.setDouble(2, produto.getPreco());
        stmt.setString(3, produto.getCaminhoImagem());
        stmt.setString(4, produto.getDescricao());
        stmt.setInt(5, produto.getUnidade().getId());

        int rows = stmt.executeUpdate();
        if (rows == 0) return -1;

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}

   @Override
public Produto buscarPorId(int id) {
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE p.id_produto = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                UnidadeMedida unidade = new UnidadeMedida(
                    rs.getInt("id_unidade"),
                    rs.getString("unidade_nome")
                );

                return new Produto(
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getDouble("preco_unitario"),
                    rs.getString("imagem_path"),
                    rs.getString("descricao"),
                    unidade
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // produto não encontrado ou erro
}

@Override
public Produto buscarPorNome(String nome) {
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE p.nome = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, nome);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                UnidadeMedida unidade = new UnidadeMedida(
                    rs.getInt("id_unidade"),
                    rs.getString("unidade_nome")
                );

                return new Produto(
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getDouble("preco_unitario"),
                    rs.getString("imagem_path"),
                    rs.getString("descricao"),
                    unidade
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


@Override
public List<Produto> listarTodos() {
    List<Produto> produtos = new ArrayList<>();
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade";

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            UnidadeMedida unidade = new UnidadeMedida(
                rs.getInt("id_unidade"),
                rs.getString("unidade_nome")
            );

            Produto produto = new Produto(
                rs.getInt("id_produto"),
                rs.getString("nome"),
                rs.getDouble("preco_unitario"),
                rs.getString("imagem_path"),
                rs.getString("descricao"),
                unidade
            );

            produtos.add(produto);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return produtos;
}

@Override
public boolean atualizar(Produto produto) {
    String sql = "UPDATE produto SET nome = ?, preco_unitario = ?, imagem_path = ?, descricao = ?, id_unidade = ? WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, produto.getNome());
        stmt.setDouble(2, produto.getPreco());
        stmt.setString(3, produto.getCaminhoImagem());
        stmt.setString(4, produto.getDescricao());
        stmt.setInt(5, produto.getUnidade().getId()); // Aqui é o ID da unidade
        stmt.setInt(6, produto.getId());
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public boolean remover(int id) {
    String sql = "DELETE FROM produto WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}
