package com.hortifacil.service;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.model.Produto;

import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private ProdutoDAO produtoDAO;

    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    public int cadastrarProduto(Produto produto) {
        // Retorna o ID do produto salvo
        return produtoDAO.salvar(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoDAO.listarTodos();
    }

  public Optional<Produto> buscarPorNome(String nome) {
    return Optional.ofNullable(produtoDAO.buscarPorNome(nome));
}

    }
