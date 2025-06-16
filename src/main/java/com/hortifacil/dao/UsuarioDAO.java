package com.hortifacil.dao;

import com.hortifacil.util.UsuarioMapper;
import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection conn;

    public UsuarioDAO(Connection conn) {
        this.conn = conn;
    }

public boolean inserirUsuario(Usuario usuario) throws SQLException {
    if (usuario.getlogin() == null || usuario.getSenha() == null) {
        throw new IllegalArgumentException("login e senha são obrigatórios.");
    }

    if (existelogin(usuario.getlogin())) {
        throw new SQLException("login já cadastrado.");
    }

    try {
        conn.setAutoCommit(false); // inicia transação

        String sqlUsuario = "INSERT INTO usuario (login, senha, tipo, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getlogin());
            String senhaHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            stmt.setString(2, senhaHash);
            stmt.setString(3, usuario.getTipo().name());
            stmt.setString(4, usuario.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                } else {
                    conn.rollback();
                    throw new SQLException("Falha ao obter ID do usuário.");
                }
            }
        }

        // Só insere na tabela cliente se for instância de Cliente
        if (usuario instanceof Cliente cliente) {
            String sqlCliente = "INSERT INTO cliente (id_usuario, cpf, nome, email, telefone) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
                stmtCliente.setInt(1, usuario.getId());
                stmtCliente.setString(2, cliente.getCpf());
                stmtCliente.setString(3, cliente.getNome());
                stmtCliente.setString(4, cliente.getEmail());
                stmtCliente.setString(5, cliente.getTelefone());
                int rows = stmtCliente.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }
        }

        conn.commit(); // confirma tudo
        return true;

    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}

    public Usuario buscarPorlogin(String login) throws SQLException {
        String sql = "SELECT u.*, c.cpf, c.nome, c.email, c.telefone FROM usuario u LEFT JOIN cliente c ON u.id_usuario = c.id_usuario WHERE u.login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UsuarioMapper.montarUsuario(rs);
                }
            }
        }
        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.*, c.cpf, c.nome, c.email, c.telefone FROM usuario u LEFT JOIN cliente c ON u.id_usuario = c.id_usuario WHERE u.id_usuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UsuarioMapper.montarUsuario(rs);
                }
            }
        }
        return null;
    }

    public boolean validarLogin(Usuario usuario, String senhaDigitada) {
        return BCrypt.checkpw(senhaDigitada, usuario.getSenha());
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, c.cpf, c.nome, c.email, c.telefone FROM usuario u LEFT JOIN cliente c ON u.id_usuario = c.id_usuario";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(UsuarioMapper.montarUsuario(rs));
            }
        }
        return usuarios;
    }

    // AtualizarUsuario fix
public boolean atualizarUsuario(Usuario usuario) throws SQLException {
    boolean atualizarSenha = usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty();

    String sqlUsuario = atualizarSenha
            ? "UPDATE usuario SET login = ?, senha = ?, tipo = ?, status = ? WHERE id_usuario = ?"
            : "UPDATE usuario SET login = ?, tipo = ?, status = ? WHERE id_usuario = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
        int idx = 1;
        stmt.setString(idx++, usuario.getlogin());

        if (atualizarSenha) {
            String senhaHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            stmt.setString(idx++, senhaHash);
        }

        stmt.setString(idx++, usuario.getTipo().name());
        stmt.setString(idx++, usuario.getStatus().name());
        stmt.setInt(idx, usuario.getId());

        int rowsUpdated = stmt.executeUpdate();
        if (rowsUpdated == 0) return false;
    }

    if (usuario instanceof Cliente cliente) {
        String sqlCliente = "UPDATE cliente SET cpf = ?, nome = ?, email = ?, telefone = ? WHERE id_usuario = ?";
        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
            stmtCliente.setString(1, cliente.getCpf());
            stmtCliente.setString(2, cliente.getNome());
            stmtCliente.setString(3, cliente.getEmail());
            stmtCliente.setString(4, cliente.getTelefone());
            stmtCliente.setInt(5, cliente.getId());  // corrigido aqui, usa getId() do Usuario
            int rowsUpdated = stmtCliente.executeUpdate();
            if (rowsUpdated == 0) return false;
        }
    }

    return true;  // retorna true só se tudo deu certo
}


    public boolean excluirUsuario(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean existelogin(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
