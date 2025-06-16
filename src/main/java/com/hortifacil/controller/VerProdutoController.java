package com.hortifacil.controller;

import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.EstoqueService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class VerProdutoController {

    @FXML private FlowPane produtosContainer;

    private EstoqueService estoqueService;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;

    @FXML
    private void initialize() {
        try {
            var conn = DatabaseConnection.getConnection();
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn);
            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDadosUsuario(String cpf, String nomeUsuario, int clienteId) {
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.clienteId = clienteId;
        carregarProdutosDoEstoque();
    }

    private void carregarProdutosDoEstoque() {
        produtosContainer.getChildren().clear();

        List<ProdutoEstoque> produtosEstoque = estoqueService.listarEstoque();

        for (ProdutoEstoque pEstoque : produtosEstoque) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProdutoCardView.fxml"));
                VBox card = loader.load();

                ProdutoCardController controller = loader.getController();
                controller.setProduto(pEstoque.getProduto());
                controller.setListener((produto, quantidade) -> adicionarAoCarrinho(produto, quantidade));


                produtosContainer.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("Erro ao carregar card do produto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

private void adicionarAoCarrinho(Produto produto, int quantidade) {
    try {
        var conn = DatabaseConnection.getConnection();
        var carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);

        carrinhoProdutoDAO.adicionarAoCarrinho(
            new CarrinhoProduto(clienteId, produto, quantidade, produto.getPreco())
        );

        System.out.println(produto.getNome() + " adicionado ao carrinho! Qtd: " + quantidade);

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

@FXML
private void handleVerCarrinho() {
    Stage stage = (Stage) produtosContainer.getScene().getWindow();

    SceneController.<CarrinhoProdutoController>trocarCenaComDados(
            stage,
            "/view/CarrinhoProdutoView.fxml",
            "Carrinho",
            controller -> controller.setDadosUsuario(cpf, nomeUsuario, clienteId)
    );
}


    @FXML
    private void handleVoltar() {
        Stage stage = (Stage) produtosContainer.getScene().getWindow();

        SceneController.<HomeClienteController>trocarCenaComDados(
                stage,
                "/view/HomeClienteView.fxml",
                "Home Cliente",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, clienteId)
        );
    }
}
