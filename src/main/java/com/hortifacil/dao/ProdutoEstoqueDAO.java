package com.hortifacil.dao;

import com.hortifacil.model.ProdutoEstoque;

import java.sql.SQLException;
import java.util.List;

public interface ProdutoEstoqueDAO {

    int adicionarLote(ProdutoEstoque lote);
    List<ProdutoEstoque> listarLotesPorProduto(int produtoId);
    boolean atualizarQuantidadeLote(int idLote, double novaQuantidade);
    boolean removerLote(int idLote);

    // Métodos que você precisa implementar para o serviço
    boolean salvar(ProdutoEstoque produtoEstoque);
    List<ProdutoEstoque> listarTodos();
    List<ProdutoEstoque> buscarPorNomeProduto(String nomeProduto) throws SQLException;
}
