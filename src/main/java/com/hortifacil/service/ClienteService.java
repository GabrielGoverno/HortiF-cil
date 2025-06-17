package com.hortifacil.service;

import com.hortifacil.dao.ClienteDAO;
import com.hortifacil.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class ClienteService {

    private ClienteDAO clienteDAO;

    private static ClienteService instance;

    private ClienteService() {
    try {
        Connection conn = DatabaseConnection.getConnection();
        clienteDAO = new ClienteDAO(conn);
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException("Falha ao criar ClienteDAO", e);
    }
}

    public static ClienteService getInstance() {
        if (instance == null) {
            instance = new ClienteService();
        }
        return instance;
    }

    public String buscarNomePorId(int idCliente) {
        return clienteDAO.buscarNomePorId(idCliente);
    }

    public String buscarEnderecoPorId(int idCliente) {
        return clienteDAO.buscarEnderecoPorId(idCliente);
    }
}
