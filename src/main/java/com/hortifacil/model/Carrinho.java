package com.hortifacil.model;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private int idCliente;
    private List<CarrinhoProduto> itens;

    public Carrinho(int idCliente) {
        this.idCliente = idCliente;
        this.itens = new ArrayList<>();
    }

    public int getIdCliente() {
        return idCliente;
    }

    public List<CarrinhoProduto> getItens() {
        return itens;
    }

    public void adicionarItem(CarrinhoProduto item) {
        // Se o produto já existe no carrinho, atualiza quantidade
        for (CarrinhoProduto i : itens) {
            if (i.getProduto().getId() == item.getProduto().getId()) {
                i.setQuantidade(i.getQuantidade() + item.getQuantidade());
                return;
            }
        }
        itens.add(item);
    }

    public void removerItem(int idProduto) {
        itens.removeIf(i -> i.getProduto().getId() == idProduto);
    }

    public void limparCarrinho() {
        itens.clear();
    }

    public double calcularTotal() {
        double total = 0;
        for (CarrinhoProduto item : itens) {
            total += item.getQuantidade() * item.getPrecoUnitario();
        }
        return total;
    }
}


