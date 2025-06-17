package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.Produto;
import com.hortifacil.service.EstoqueService;
import com.hortifacil.service.ProdutoService;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdicionarEstoqueController {

    @FXML
    private TableView<ProdutoEstoque> tvEstoque;

    @FXML
    private TableColumn<ProdutoEstoque, String> colProduto;

    @FXML
    private TableColumn<ProdutoEstoque, LocalDate> colDataColheita;

    @FXML
    private TableColumn<ProdutoEstoque, LocalDate> colDataValidade;

    @FXML
    private ComboBox<Produto> cbProduto;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private TableColumn<ProdutoEstoque, Integer> colQuantidade;


    @FXML
    private DatePicker dpDataColhido;

    @FXML
    private DatePicker dpDataValidade;

    @FXML
    private Button btnAdicionar;

    @FXML
    private Button btnVoltarHome;

    @FXML
    private Label lblInfo;

    private EstoqueService estoqueService;
    private ProdutoService produtoService;
@FXML
public void initialize() {
    try {
        Connection conn = DatabaseConnection.getConnection();

        ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
        ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, produtoDAO);

        produtoService = new ProdutoService(produtoDAO);
        estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

        cbProduto.getItems().addAll(produtoService.listarProdutos());

        colProduto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduto().getNome()));

        colQuantidade.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getQuantidade()).asObject());

        colDataColheita.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getDataColhido()));

        colDataValidade.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getDataValidade()));

        atualizarListaEstoque();

        btnAdicionar.setOnAction(e -> adicionarProduto());
        btnVoltarHome.setOnAction(e -> voltarHome());

    } catch (Exception e) {
        e.printStackTrace();
        lblInfo.setText("Erro ao iniciar o controlador: " + e.getMessage());
    }
}

private void adicionarProduto() {
    try {
        Produto produtoSelecionado = cbProduto.getValue();
        if (produtoSelecionado == null) {
            lblInfo.setText("Selecione um produto.");
            return;
        }

        int quantidade = Integer.parseInt(txtQuantidade.getText());
        if (quantidade <= 0) {
            lblInfo.setText("Informe uma quantidade válida.");
            return;
        }

        LocalDate dataColheita = dpDataColhido.getValue();
        if (dataColheita == null) {
            lblInfo.setText("Informe a data de colheita.");
            return;
        }

        LocalDate dataValidade = dpDataValidade.getValue();
        if (dataValidade == null) {
            lblInfo.setText("Informe a data de validade.");
            return;
        }

       ProdutoEstoque estoque = new ProdutoEstoque(produtoSelecionado, quantidade, dataColheita, dataValidade);
        estoqueService.adicionarProduto(estoque);

        atualizarListaEstoque();

        lblInfo.setText("Produto adicionado com sucesso!");
        cbProduto.setValue(null);
        txtQuantidade.clear();
        dpDataColhido.setValue(null);
        dpDataValidade.setValue(null);

    } catch (NumberFormatException e) {
        lblInfo.setText("Quantidade inválida.");
    } catch (Exception e) {
        e.printStackTrace();
        lblInfo.setText("Erro ao adicionar produto: " + e.getMessage());
    }
}

    private void atualizarListaEstoque() {
    try {
        tvEstoque.getItems().clear();
        tvEstoque.getItems().addAll(estoqueService.listarEstoque());
    } catch (SQLException e) {
        e.printStackTrace();
        lblInfo.setText("Erro ao listar estoque: " + e.getMessage());
    }
}

    private void voltarHome() {
        Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/HomeAdminView.fxml", "home");
    }
}
