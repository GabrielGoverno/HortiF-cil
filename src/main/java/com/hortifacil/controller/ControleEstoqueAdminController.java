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

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.time.LocalDate;

public class ControleEstoqueAdminController {

    @FXML
    private ListView<ProdutoEstoque> lvEstoque;

    @FXML
    private ComboBox<Produto> cbProduto;

    @FXML
    private TextField txtQuantidade;

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
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn);

            produtoService = new ProdutoService(produtoDAO); // precisa implementar ProdutoService
            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

            // Carregar produtos no ComboBox cbProduto
            cbProduto.getItems().addAll(produtoService.listarProdutos());

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

            double quantidade = Double.parseDouble(txtQuantidade.getText());
            if (quantidade <= 0) {
                lblInfo.setText("Informe uma quantidade válida.");
                return;
            }

            LocalDate dataColheita = dpDataColhido.getValue();
            if (dataColheita == null) {
                lblInfo.setText("Informe a data de colheita.");
                return;
            }

            // Assumindo que ProdutoEstoque tem um construtor que aceita unidade como String ou converta para UnidadeMedida
          LocalDate dataValidade = dpDataValidade.getValue();
            if (dataValidade == null) {
                lblInfo.setText("Informe a data de validade.");
                return;
            }

            ProdutoEstoque novoProdutoEstoque = new ProdutoEstoque(produtoSelecionado, quantidade, dataColheita, dataValidade);

            // Ajuste conforme sua classe ProdutoEstoque, adicione data vencimento se quiser

            estoqueService.adicionarProduto(novoProdutoEstoque);

            atualizarListaEstoque();

            lblInfo.setText("Produto adicionado com sucesso!");

            // Limpar campos
            cbProduto.setValue(null);
            txtQuantidade.clear();
            dpDataColhido.setValue(null);

        } catch (NumberFormatException e) {
            lblInfo.setText("Quantidade inválida.");
        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void atualizarListaEstoque() {
        lvEstoque.getItems().clear();
        lvEstoque.getItems().addAll(estoqueService.listarEstoque());
    }

    private void voltarHome() {
        // Fecha a janela atual para voltar à anterior, ou implemente navegação conforme seu sistema
        Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/HomeAdminView.fxml", "home");
    }
}
