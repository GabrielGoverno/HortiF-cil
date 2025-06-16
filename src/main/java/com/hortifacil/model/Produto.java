package com.hortifacil.model;

public class Produto {
    private int id;
    private String nome;
    private double preco;
    private String caminhoImagem;
    private String descricao;
     private UnidadeMedida unidade;

  public Produto(int id, String nome, double preco, String caminhoImagem, String descricao, UnidadeMedida unidade) {
    this.id = id;
    this.nome = nome;
    this.preco = preco;
    this.caminhoImagem = caminhoImagem;
    this.descricao = descricao;
    this.unidade = unidade;
}

@Override
    public String toString() {
        return nome; // ou qualquer atributo que queira exibir, como nome + categoria, etc.
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getCaminhoImagem() { return caminhoImagem; }
    public void setCaminhoImagem(String caminhoImagem) { this.caminhoImagem = caminhoImagem; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public UnidadeMedida getUnidade() {return unidade; }
    public String getNomeUnidade() {return unidade != null ? unidade.getNome() : "";}
}
