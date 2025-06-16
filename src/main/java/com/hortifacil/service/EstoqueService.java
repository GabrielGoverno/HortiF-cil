package com.hortifacil.service;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;

import java.util.List;

import java.util.PriorityQueue;
import java.sql.SQLException;
import java.util.Comparator;

public class EstoqueService {

    private static EstoqueService instance;

    private final ProdutoEstoqueDAO produtoEstoqueDAO;
    private final ProdutoDAO produtoDAO;

    private EstoqueService(ProdutoDAO produtoDAO, ProdutoEstoqueDAO produtoEstoqueDAO) {
    this.produtoDAO = produtoDAO;
    this.produtoEstoqueDAO = produtoEstoqueDAO;
}

public static EstoqueService getInstance(ProdutoDAO produtoDAO, ProdutoEstoqueDAO produtoEstoqueDAO) {
    if (instance == null) {
        instance = new EstoqueService(produtoDAO, produtoEstoqueDAO);
    }
    return instance;
}

     public boolean verificarEstoque(String nomeProduto, double quantidadeNecessaria) {
    int produtoId = buscarProdutoIdPorNome(nomeProduto);
    if (produtoId == -1) {
        return false; // produto não encontrado
    }

    List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);

    double totalDisponivel = 0.0;
    for (ProdutoEstoque lote : lotes) {
        totalDisponivel += lote.getQuantidade();
        if (totalDisponivel >= quantidadeNecessaria) {
            return true; // estoque suficiente
        }
    }
    return false; // estoque insuficiente
}

     public boolean removerProdutosDoEstoque(List<CarrinhoProduto> itens) {
        for (CarrinhoProduto item : itens) {
            boolean sucesso = consumirProdutoPorValidade(item.getProduto().getNome(), item.getQuantidade());
            if (!sucesso) {
                return false;
            }
        }
        return true;
    }


    private boolean consumirProdutoPorValidade(String nomeProduto, double quantidadeNecessaria) {
        int produtoId = buscarProdutoIdPorNome(nomeProduto);
        List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);

        PriorityQueue<ProdutoEstoque> fila = new PriorityQueue<>(
            Comparator.comparing(ProdutoEstoque::getDataValidade)
        );
        fila.addAll(lotes);

        while (!fila.isEmpty() && quantidadeNecessaria > 0) {
            ProdutoEstoque lote = fila.poll();
            double disponivel = lote.getQuantidade();

            if (disponivel >= quantidadeNecessaria) {
                double novaQuantidade = disponivel - quantidadeNecessaria;
                produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), novaQuantidade);
                quantidadeNecessaria = 0;
            } else {
                produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), 0);
                quantidadeNecessaria -= disponivel;
            }
        }

        return quantidadeNecessaria <= 0;
    }

    public int buscarProdutoIdPorNome(String nomeProduto) {
            Produto produto = produtoDAO.buscarPorNome(nomeProduto);
            return (produto != null) ? produto.getId() : -1;
        }

public void adicionarProduto(ProdutoEstoque produtoEstoque) {
    produtoEstoqueDAO.salvar(produtoEstoque); // Ou o método equivalente no DAO
}

public List<ProdutoEstoque> listarEstoque() {
    return produtoEstoqueDAO.listarTodos(); // Implemente listarTodos() no DAO
}

public ProdutoEstoque buscarProdutoEstoquePorNome(String nomeProduto) {
    try {
        List<ProdutoEstoque> lotes = produtoEstoqueDAO.buscarPorNomeProduto(nomeProduto);
        if (lotes != null && !lotes.isEmpty()) {
            return lotes.get(0);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

}
