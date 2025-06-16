package com.hortifacil.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.UnidadeMedidaDAO;
import com.hortifacil.dao.UnidadeMedidaDAOImpl;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;
import com.hortifacil.service.ProdutoService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;

public class CadastroProdutoController {

    @FXML private TextField nomeField;
    @FXML private TextField precoField;
    @FXML private TextField imagemField;
    @FXML private TextArea descricaoArea;
    @FXML private Button salvarBtn;
    @FXML private ComboBox<UnidadeMedida> unidadeComboBox;
    @FXML private Label mensagemLabel;

    private ProdutoService produtoService;
    private UnidadeMedidaDAO unidadeMedidaDAO;

    public void setProdutoService(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public void setUnidadeMedidaDAO(UnidadeMedidaDAO dao) {
    this.unidadeMedidaDAO = dao;
    }

  @FXML
private void initialize() {
    try {
        Connection conn = DatabaseConnection.getConnection();
        
        // Instancia DAOs
        unidadeMedidaDAO = new UnidadeMedidaDAOImpl(conn);
        ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
        
        // Instancia Service passando o DAO no construtor
        produtoService = new ProdutoService(produtoDAO);

        List<UnidadeMedida> unidades = unidadeMedidaDAO.listarTodas();
        unidadeComboBox.getItems().clear();
        unidadeComboBox.getItems().addAll(unidades);
        if (!unidades.isEmpty()) {
            unidadeComboBox.getSelectionModel().selectFirst();
        }

        salvarBtn.setOnAction(event -> salvarProduto());
    } catch (SQLException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Conexão");
        alert.setHeaderText("Não foi possível conectar ao banco de dados.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    precoField.textProperty().addListener((obs, oldVal, newVal) -> {
    if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
        precoField.setText(oldVal);
    }
});

}


    private void salvarProduto() {
    try {
        String nome = nomeField.getText().trim();
        String precoTexto = precoField.getText().trim();
        String caminhoImagem = imagemField.getText().trim();
        String descricao = descricaoArea.getText().trim();
        UnidadeMedida unidade = unidadeComboBox.getSelectionModel().getSelectedItem();

        // Validações
        if (nome.isEmpty()) {
            setMensagem("O nome do produto é obrigatório.", "red");
            return;
        }

        if (precoTexto.isEmpty()) {
            setMensagem("O preço é obrigatório.", "red");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoTexto);
            if (preco <= 0) {
                setMensagem("O preço deve ser maior que zero.", "red");
                return;
            }
        } catch (NumberFormatException e) {
            setMensagem("Preço inválido. Use apenas números e ponto para decimais.", "red");
            return;
        }

        if (descricao.isEmpty()) {
            setMensagem("A descrição é obrigatória.", "red");
            return;
        }

        if (unidade == null) {
            setMensagem("Selecione uma unidade de medida.", "red");
            return;
        }

        Produto produto = new Produto(0, nome, preco, caminhoImagem, descricao, unidade);
        int id = produtoService.cadastrarProduto(produto);

        setMensagem("Produto cadastrado com sucesso! ID: " + id, "green");
        limparCampos();
    } catch (Exception e) {
        e.printStackTrace();
        setMensagem("Erro ao salvar o produto: " + e.getMessage(), "red");
    }
}

private void setMensagem(String texto, String cor) {
    mensagemLabel.setText(texto);
    mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
}

    private void limparCampos() {
        nomeField.clear();
        precoField.clear();
        imagemField.clear();
        descricaoArea.clear();
    }

@FXML
private void handleVoltar(ActionEvent event) {
    SceneController.trocarCena(event, "/view/HomeAdminView.fxml", "Home Admin");
}

@FXML
private void handleSalvar(ActionEvent event) {
    salvarProduto();
}

}