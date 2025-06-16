package com.hortifacil.model;

public class UnidadeMedida {
    private int id;
    private String nome;

    public UnidadeMedida(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }

    @Override
    public String toString() {
        return nome;
    }
}
