package com.hortifacil.model;

public class ProdutoEstoqueResumo {
    private Produto produto;
    private int quantidadeTotal;

    public ProdutoEstoqueResumo(Produto produto, int quantidadeTotal) {
        this.produto = produto;
        this.quantidadeTotal = quantidadeTotal;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }
}