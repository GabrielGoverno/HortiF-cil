package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import java.util.List;

public interface ProdutoDAO {
    Produto buscarPorId(int id);
    Produto buscarPorNome(String nome);
    List<Produto> listarTodos();
    boolean atualizar(Produto produto);
    boolean remover(int id);
    int salvar(Produto produto);
}
