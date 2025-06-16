package com.hortifacil.util;

import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioMapper {

    public static Usuario montarUsuario(ResultSet rs) throws SQLException {
        Enums.TipoUsuario tipo = Enums.TipoUsuario.valueOf(rs.getString("tipo").toUpperCase());
        if (tipo == Enums.TipoUsuario.CLIENTE) {
            Cliente cliente = new Cliente();
            cliente.setId(rs.getInt("id_usuario"));
            cliente.setlogin(rs.getString("login"));
            cliente.setSenha(rs.getString("senha"));
            cliente.setTipo(tipo);
            cliente.setStatus(Enums.StatusUsuario.valueOf(rs.getString("status").toUpperCase()));
            cliente.setCpf(rs.getString("cpf"));
            cliente.setNome(rs.getString("nome"));
            cliente.setEmail(rs.getString("email"));
            return cliente;
        } else {
            Usuario usuario = new Usuario();
            usuario.setId(rs.getInt("id_usuario"));
            usuario.setlogin(rs.getString("login"));
            usuario.setSenha(rs.getString("senha"));
            usuario.setTipo(tipo);
            usuario.setStatus(Enums.StatusUsuario.valueOf(rs.getString("status").toUpperCase()));
            return usuario;
        }
    }

}

