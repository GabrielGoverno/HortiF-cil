package com.hortifacil.model;

public class CarrinhoProduto {
    private int id;
    private int clienteId;
    private Produto produto;
    private int quantidade;
    private double precoUnitario;

    public CarrinhoProduto() {
    }

  public CarrinhoProduto(int id, int clienteId, Produto produto, int quantidade, double precoUnitario) {
    this.id = id;
    this.clienteId = clienteId;
    this.produto = produto;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
}

    public CarrinhoProduto(int clienteId, Produto produto, int quantidade, double precoUnitario) {
        this(0, clienteId, produto, quantidade, precoUnitario);
    }

    // Getters e Setters
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public double getTotal() {return quantidade * precoUnitario; }
}
