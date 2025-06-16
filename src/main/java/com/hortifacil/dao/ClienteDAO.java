package com.hortifacil.dao;

import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;
import com.hortifacil.util.Enums;

import java.sql.*;

import org.mindrot.jbcrypt.BCrypt; // biblioteca para hash de senha

public class ClienteDAO {

    private Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;
    }

    // Insere só o usuário e retorna o ID gerado
    public int inserirUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (login, senha, tipo, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getlogin());
            String senhaHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            stmt.setString(2, senhaHash);
            stmt.setString(3, usuario.getTipo().name());
            stmt.setString(4, usuario.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuário, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Falha ao obter o ID do usuário inserido.");
                }
            }
        }
    }

    public int inserirCliente(Cliente cliente) throws SQLException {
    String sql = "INSERT INTO cliente (id_usuario, cpf, nome, email, telefone) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, cliente.getId()); // id_usuario
        stmt.setString(2, cliente.getCpf());
        stmt.setString(3, cliente.getNome());
        stmt.setString(4, cliente.getEmail());
        stmt.setString(5, cliente.getTelefone());

        int rows = stmt.executeUpdate();
        if (rows == 0) return -1;

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1); // retorna id_cliente
            }
        }
        return -1;
    }
}

    public Cliente buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT u.id_usuario, u.login, u.senha, u.tipo, u.status, " +
                     "c.cpf, c.nome, c.email, c.telefone " +
                     "FROM usuario u JOIN cliente c ON u.id_usuario = c.id_usuario " +
                     "WHERE c.cpf = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirClienteCompleto(rs);
                }
            }
        }
        return null;

    }

   public int inserirClienteCompleto(Cliente cliente) throws SQLException {
    String sqlUsuario = "INSERT INTO usuario (login, senha, tipo, status) VALUES (?, ?, ?, ?)";
    String sqlCliente = "INSERT INTO cliente (id_usuario, nome, cpf, email, telefone) VALUES (?, ?, ?, ?, ?)";

    try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmtUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
            stmtUser.setString(1, cliente.getlogin());
            stmtUser.setString(2, BCrypt.hashpw(cliente.getSenha(), BCrypt.gensalt()));
            stmtUser.setString(3, cliente.getTipo().name());
            stmtUser.setString(4, cliente.getStatus().name());

            int rows = stmtUser.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            try (ResultSet rs = stmtUser.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1)); // id_usuario
                } else {
                    conn.rollback();
                    return -1;
                }
            }
        }

        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
            stmtCliente.setInt(1, cliente.getId());
            stmtCliente.setString(2, cliente.getNome());
            stmtCliente.setString(3, cliente.getCpf());
            stmtCliente.setString(4, cliente.getEmail());
            stmtCliente.setString(5, cliente.getTelefone());

            int rows = stmtCliente.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            try (ResultSet rs = stmtCliente.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // retorna o id_cliente
                }
            }
        }

        conn.commit();
        return -1; // falhou ao obter id_cliente
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}

 private Cliente construirClienteCompleto(ResultSet rs) throws SQLException {
    Cliente cliente = new Cliente();
    cliente.setId(rs.getInt("id_usuario"));
    cliente.setlogin(rs.getString("login"));
    cliente.setSenha(rs.getString("senha"));
    cliente.setTipo(Enums.TipoUsuario.valueOf(rs.getString("tipo").toUpperCase()));
    cliente.setStatus(Enums.StatusUsuario.valueOf(rs.getString("status").toUpperCase()));

    cliente.setCpf(rs.getString("cpf"));
    cliente.setNome(rs.getString("nome"));
    cliente.setEmail(rs.getString("email"));
    cliente.setTelefone(rs.getString("telefone"));

    return cliente;
}

public Cliente buscarPorIdCliente(int idCliente) {
    String sql = "SELECT nome, endereco FROM cliente WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Cliente cliente = new Cliente();
            cliente.setNome(rs.getString("nome"));
            return cliente;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
    
}

    public String buscarNomePorId(int idCliente) {
    String sql = "SELECT nome FROM cliente WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("nome");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Desconhecido";
}


    public String buscarEnderecoPorId(int idCliente) {
    String sql = "SELECT rua, numero, bairro, complemento FROM endereco WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return String.format(
                "%s, %s, %s - %s/%s %s",
                rs.getString("rua"),
                rs.getString("numero"),
                rs.getString("bairro"),
                rs.getString("complemento") != null ? "(" + rs.getString("complemento") + ")" : ""
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Endereço não cadastrado";
}

}
