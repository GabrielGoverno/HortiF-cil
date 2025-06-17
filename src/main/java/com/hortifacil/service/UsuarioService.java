package com.hortifacil.service;

import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;
import com.hortifacil.util.Enums.ResultadoLogin;
import com.hortifacil.dao.UsuarioDAO;
import com.hortifacil.database.DatabaseConnection;

import com.hortifacil.util.Enums;

import java.sql.Connection;
import java.sql.SQLException;

public class UsuarioService {

    private final Connection conn;
    private final UsuarioDAO usuarioDAO;

    public UsuarioService() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
        this.usuarioDAO = new UsuarioDAO(conn);
    }

    public ResultadoLogin autenticar(String Login, String senha) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorLogin(Login);
    if (usuario == null) return ResultadoLogin.USUARIO_NAO_ENCONTRADO;
    if (usuario.getStatus() != Enums.StatusUsuario.ATIVO) return ResultadoLogin.USUARIO_INATIVO;
    if (!usuarioDAO.validarLogin(usuario, senha)) return ResultadoLogin.SENHA_INVALIDA;
    return ResultadoLogin.SUCESSO;
    }

    public Usuario getUsuarioPorLogin(String Login) throws SQLException {
        return usuarioDAO.buscarPorLogin(Login);
    }

    public boolean cadastrarUsuario(Usuario usuario) {
        try {
            validarUsuario(usuario);

            if (usuarioDAO.buscarPorLogin(usuario.getLogin()) != null) {
                System.out.println("Usuário já cadastrado: " + usuario.getLogin());
                return false;
            }

            return usuarioDAO.inserirUsuario(usuario);
        } catch (Exception e) {
            System.err.println("Erro ao cadastrar usuário: " + e.getMessage());
            return false;
        }
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario.getLogin() == null || usuario.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
        }

        if (usuario.getStatus() == null) {
            usuario.setStatus(Enums.StatusUsuario.ATIVO);
        }

        if (usuario instanceof Cliente cliente) {
            validarCliente(cliente);
        }
    }

    private void validarCliente(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório para cliente.");
        }
        if (cliente.getEmail() == null || !cliente.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido para cliente.");
        }
        if (cliente.getCpf() == null || !cliente.getCpf().matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido para cliente.");
        }
    }
}
