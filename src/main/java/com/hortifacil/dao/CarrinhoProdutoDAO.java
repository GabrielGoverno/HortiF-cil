package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import java.util.List;

public interface CarrinhoProdutoDAO {
    boolean adicionarAoCarrinho(CarrinhoProduto item);
    List<CarrinhoProduto> listarPorCliente(int clienteId);
    void remover(int id);
    boolean limparCarrinhoDoCliente(int clienteId);

    // Adicione esses dois métodos para corrigir o erro
    boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade);
    void removerItem(int clienteId, int produtoId);
}
