package com.hortifacil.controller;

import com.hortifacil.dao.*;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ProdutoQuantidadeTotal;
import com.hortifacil.service.EstoqueService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class EstoqueResumoController {

    @FXML
    private TableView<ProdutoQuantidadeTotal> tvResumoEstoque;

    @FXML
    private TableColumn<ProdutoQuantidadeTotal, String> colProduto;

    @FXML
    private TableColumn<ProdutoQuantidadeTotal, Integer> colQuantidade;

    private EstoqueService estoqueService;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, produtoDAO);

            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

            colProduto.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduto().getNome()));
            colQuantidade.setCellValueFactory(cellData ->
            new SimpleIntegerProperty((int) cellData.getValue().getQuantidadeTotal()).asObject());


            atualizarResumo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atualizarResumo() {
        try {
            tvResumoEstoque.getItems().setAll(estoqueService.listarProdutosComQuantidadeTotalAgrupada());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void voltar() {
        Stage stage = (Stage) tvResumoEstoque.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/HomeAdminView.fxml", "Home Admin");
    }
}
