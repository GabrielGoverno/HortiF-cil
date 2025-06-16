package com.hortifacil.model;

import com.hortifacil.util.Enums;

public class Usuario {

    private int id;
    private String login;
    private String senha;
    private Enums.TipoUsuario tipo;
    private Enums.StatusUsuario status = Enums.StatusUsuario.ATIVO;

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getlogin() { return login; }
    public void setlogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Enums.TipoUsuario getTipo() { return tipo; }
    public void setTipo(Enums.TipoUsuario tipo) { this.tipo = tipo; }

    public Enums.StatusUsuario getStatus() { return status; }
    public void setStatus(Enums.StatusUsuario status) { this.status = status; }

    public Usuario() {}

    public Usuario(int id, String login, String senha, Enums.TipoUsuario tipo, Enums.StatusUsuario status) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.tipo = tipo;
        this.status = status;
    }
}
