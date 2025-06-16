package com.hortifacil.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import com.hortifacil.model.UnidadeMedida;

public class UnidadeMedidaDAOImpl implements UnidadeMedidaDAO {
    private final Connection connection;

    public UnidadeMedidaDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<UnidadeMedida> listarTodas() {
        List<UnidadeMedida> unidades = new ArrayList<>();
        String sql = "SELECT id_unidade, nome FROM unidade_medida";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                unidades.add(new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unidades;
    }
}
