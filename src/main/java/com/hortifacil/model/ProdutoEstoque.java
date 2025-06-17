package com.hortifacil.model;

import java.time.LocalDate;

public class ProdutoEstoque {
    private int id;
    private Produto produto;
    private int quantidade; 
    private LocalDate dataColhido;
    private LocalDate dataValidade;

    public ProdutoEstoque(int id, Produto produto, int quantidade, LocalDate dataColhido, LocalDate dataValidade) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataColhido = dataColhido;
        this.dataValidade = dataValidade;
    }

    public ProdutoEstoque(Produto produto, int quantidade, LocalDate dataColhido, LocalDate dataValidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataColhido = dataColhido;
        this.dataValidade = dataValidade;
    }

    public ProdutoEstoque(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataColhido = null;
        this.dataValidade = null;
    }

    public ProdutoEstoque() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDate getDataColhido() { return dataColhido; }
    public void setDataColhido(LocalDate dataColhido) { this.dataColhido = dataColhido; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

   @Override
public String toString() {
    String nome = (produto != null) ? produto.getNome() : "Produto";
    String unidade = (produto != null && produto.getUnidade() != null) ? produto.getUnidade().getNome() : "";
    String preco = (produto != null) ? String.format("R$%.2f", produto.getPreco()) : "R$0.00";
    String colhido = (dataColhido != null) ? dataColhido.toString() : "N/A";
    String validade = (dataValidade != null) ? dataValidade.toString() : "N/A";

    return nome + " - Qtde: " + quantidade + " " + unidade +
           " - Colhido: " + colhido +
           " - Vence: " + validade +
           " - Preço: " + preco;
}

}