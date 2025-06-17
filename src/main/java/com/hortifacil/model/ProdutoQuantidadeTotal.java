package com.hortifacil.model;

public class ProdutoQuantidadeTotal {
    private Produto produto;
    private double quantidadeTotal;

    public ProdutoQuantidadeTotal(Produto produto, double quantidadeTotal) {
        this.produto = produto;
        this.quantidadeTotal = quantidadeTotal;
    }

    public Produto getProduto() {
        return produto;
    }

    public double getQuantidadeTotal() {
        return quantidadeTotal;
    }
}
