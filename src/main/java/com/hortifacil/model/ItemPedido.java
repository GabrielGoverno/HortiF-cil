package com.hortifacil.model;

import javafx.beans.property.*;

public class ItemPedido {

    private final StringProperty nomeProduto = new SimpleStringProperty();
    private final IntegerProperty quantidade = new SimpleIntegerProperty();
    private final DoubleProperty precoUnitario = new SimpleDoubleProperty();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();

    public ItemPedido(String nomeProduto, int quantidade, double precoUnitario) {
        this.nomeProduto.set(nomeProduto);
        this.quantidade.set(quantidade);
        this.precoUnitario.set(precoUnitario);

        // Bind do subtotal (quantidade * precoUnitario)
        this.subtotal.bind(this.quantidade.multiply(this.precoUnitario));
    }

    public String getNomeProduto() {
        return nomeProduto.get();
    }

    public int getQuantidade() {
        return quantidade.get();
    }

    public double getPrecoUnitario() {
        return precoUnitario.get();
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public StringProperty nomeProdutoProperty() {
        return nomeProduto;
    }

    public IntegerProperty quantidadeProperty() {
        return quantidade;
    }

    public DoubleProperty precoUnitarioProperty() {
        return precoUnitario;
    }

    public DoubleProperty subtotalProperty() {
        return subtotal;
    }
}
