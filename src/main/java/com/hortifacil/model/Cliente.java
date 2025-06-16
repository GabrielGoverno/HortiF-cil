package com.hortifacil.model;

import com.hortifacil.util.Enums;

public class Cliente extends Usuario {

    private String nome;
    private String cpf;
    private String email;
    private String telefone;

    public Cliente() {
        super();
    }

    public Cliente(int id, String login, String senha, Enums.TipoUsuario tipo, Enums.StatusUsuario status,
                   String nome, String cpf, String email, String telefone) {
        super(id, login, senha, tipo, status);
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }

    // getters e setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
