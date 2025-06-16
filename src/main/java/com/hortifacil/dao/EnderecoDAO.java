package com.hortifacil.dao;

import com.hortifacil.model.Endereco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EnderecoDAO {

    private Connection conn;

    public EnderecoDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean inserir(Endereco endereco) {
        String sql = "INSERT INTO endereco (rua, numero, bairro, complemento, id_cliente) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setString(1, endereco.getRua());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getBairro());
            stmt.setString(4, endereco.getComplemento());
            stmt.setInt(5, endereco.getClienteId());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir endereço: " + e.getMessage(), e);
        }
    }
}
