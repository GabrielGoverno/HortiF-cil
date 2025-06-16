package com.hortifacil.model;

import java.time.LocalDate;

public class ProdutoEstoque {
    private int id;
    private Produto produto;
    private double quantidade;
    private LocalDate dataColhido;
    private LocalDate dataValidade;

    public ProdutoEstoque(int id, Produto produto, double quantidade, LocalDate dataColhido, LocalDate dataValidade) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataColhido = dataColhido;
        this.dataValidade = dataValidade;
    }

    public ProdutoEstoque(Produto produto, double quantidade, LocalDate dataColhido, LocalDate dataValidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataColhido = dataColhido;
        this.dataValidade = dataValidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    public LocalDate getDataColhido() { return dataColhido; }
    public LocalDate getDataValidade() { return dataValidade; }

    @Override
    public String toString() {
        return produto.getNome()
            + " - Qtde: " + String.format("%.2f", quantidade) + " " + produto.getUnidade().getNome()
            + " - Colhido: " + dataColhido
            + " - Vence: " + dataValidade
            + " - Preço: R$" + String.format("%.2f", produto.getPreco());
    }
}
