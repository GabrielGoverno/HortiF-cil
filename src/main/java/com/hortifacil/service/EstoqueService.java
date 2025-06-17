package com.hortifacil.service;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.ProdutoQuantidadeTotal;

import java.sql.SQLException;
import java.util.*;

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

    public void adicionarProduto(ProdutoEstoque novoProduto) throws SQLException {
        ProdutoEstoque loteExistente = produtoEstoqueDAO.buscarPorProdutoData(
            novoProduto.getProduto().getId(),
            novoProduto.getDataColhido(),
            novoProduto.getDataValidade()
        );

        if (loteExistente != null) {
            int novaQuantidade = loteExistente.getQuantidade() + novoProduto.getQuantidade();
            loteExistente.setQuantidade(novaQuantidade);
            produtoEstoqueDAO.atualizarQuantidadeLote(loteExistente.getId(), novaQuantidade);
        } else {
            produtoEstoqueDAO.salvar(novoProduto);
        }
    }

    public List<ProdutoEstoque> listarEstoque() throws SQLException {
        return produtoEstoqueDAO.listarTodos();
    }

    public List<ProdutoEstoque> listarProdutosComQuantidadeTotal() throws SQLException {
        List<ProdutoEstoque> todosLotes = produtoEstoqueDAO.listarTodos();
        Map<Integer, ProdutoEstoque> agrupado = new HashMap<>();

        for (ProdutoEstoque lote : todosLotes) {
            int produtoId = lote.getProduto().getId();

            if (agrupado.containsKey(produtoId)) {
                ProdutoEstoque existente = agrupado.get(produtoId);
                existente.setQuantidade(existente.getQuantidade() + lote.getQuantidade());
            } else {
                agrupado.put(produtoId, new ProdutoEstoque(
                    lote.getProduto(),
                    lote.getQuantidade()
                ));
            }
        }

        return new ArrayList<>(agrupado.values());
    }

    public int buscarProdutoIdPorNome(String nomeProduto) throws SQLException {
        Produto produto = produtoDAO.buscarPorNome(nomeProduto);
        return (produto != null) ? produto.getId() : -1;
    }

    public ProdutoEstoque buscarProdutoEstoquePorNome(String nomeProduto) throws SQLException {
        int produtoId = buscarProdutoIdPorNome(nomeProduto);
        if (produtoId == -1) return null;

        List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);
        if (lotes != null && !lotes.isEmpty()) {
            lotes.sort(Comparator.comparing(ProdutoEstoque::getDataValidade));
            return lotes.get(0);
        }
        return null;
    }

 public boolean verificarEstoque(String nomeProduto, int quantidadeSolicitada) throws SQLException {
        int quantidadeDisponivel = produtoEstoqueDAO.getQuantidadeEstoquePorNome(nomeProduto);
        return quantidadeDisponivel >= quantidadeSolicitada;
    }

    public boolean removerProdutosDoEstoque(List<CarrinhoProduto> carrinho) throws SQLException {
        boolean sucesso = true;
        for (CarrinhoProduto item : carrinho) {
            int quantidadeRemover = item.getQuantidade();
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(item.getProduto().getId());

            for (ProdutoEstoque lote : lotes) {
                if (quantidadeRemover <= 0) break;

                int qtdLote = lote.getQuantidade();
                if (qtdLote > quantidadeRemover) {
                    int novaQtd = qtdLote - quantidadeRemover;
                    sucesso &= produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), novaQtd);
                    quantidadeRemover = 0;
                } else {
                    sucesso &= produtoEstoqueDAO.removerLote(lote.getId());
                    quantidadeRemover -= qtdLote;
                }
            }

            if (quantidadeRemover > 0) {
                sucesso = false;
                break;
            }
        }
        return sucesso;
    }

public List<ProdutoQuantidadeTotal> listarProdutosComQuantidadeTotalAgrupada() throws SQLException {
    List<ProdutoEstoque> todosEstoques = produtoEstoqueDAO.listarTodos();
    Map<Integer, ProdutoQuantidadeTotal> map = new HashMap<>();

    for (ProdutoEstoque pe : todosEstoques) {
        int produtoId = pe.getProduto().getId();
        ProdutoQuantidadeTotal pqt = map.get(produtoId);

        if (pqt == null) {
            pqt = new ProdutoQuantidadeTotal(pe.getProduto(), 0);
        }

        int novaQuantidade = (int) (pqt.getQuantidadeTotal() + pe.getQuantidade());
        map.put(produtoId, new ProdutoQuantidadeTotal(pe.getProduto(), novaQuantidade));
    }

    return new ArrayList<>(map.values());
}

public Produto buscarProdutoPorNomeQuantidade(String nomeProduto, int quantidadeNecessaria) throws SQLException {
    Produto produto = produtoDAO.buscarPorNome(nomeProduto);
    if (produto == null) return null;

    int quantidadeDisponivel = produtoEstoqueDAO.getQuantidadeEstoquePorNome(nomeProduto);

    if (quantidadeDisponivel >= quantidadeNecessaria) {
        return produto;
    }

    return null;
}

public Produto buscarProdutoPorNome(String nomeProduto) throws SQLException {
    return produtoDAO.buscarPorNome(nomeProduto);
}

public boolean removerProdutosDoEstoqueFIFO(List<CarrinhoProduto> itens) throws SQLException {
    try (var conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);

        for (CarrinhoProduto item : itens) {
            int qtdRestante = item.getQuantidade();
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(item.getProduto().getId());

            for (ProdutoEstoque lote : lotes) {
                int qtdLote = lote.getQuantidade();

                if (qtdLote >= qtdRestante) {
                    int novaQtd = qtdLote - qtdRestante;
                    produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), novaQtd);  // CORRETO
                    qtdRestante = 0;
                    break;
                } else {
                    produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), 0);  // remove tudo do lote
                    qtdRestante -= qtdLote;
                }
            }

            if (qtdRestante > 0) {
                conn.rollback();
                return false; // estoque insuficiente
            }
        }

        conn.commit();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}
