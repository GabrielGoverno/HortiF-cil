package com.hortifacil.controller;

import com.hortifacil.model.CarrinhoProduto;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.util.List;

public class PedidoFinalizadoController {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private VBox dadosPedidoContainer;

    @FXML
    private Label lblNumeroPedido;

    @FXML
    private Label lblDataPedido;

    @FXML
    private TableView<CarrinhoProduto> tabelaProdutos;

    @FXML
    private TableColumn<CarrinhoProduto, String> colProduto;

    @FXML
    private TableColumn<CarrinhoProduto, Integer> colQuantidade;

    @FXML
    private TableColumn<CarrinhoProduto, Double> colPreco;

    @FXML
    private TableColumn<CarrinhoProduto, Double> colSubtotal;

    @FXML
    private Label lblTotalPedido;

    private int pedidoId;
    private List<CarrinhoProduto> itensPedido;
    private String dataPedido;
    private double totalPedido;
    private String cpf;
    private String nomeUsuario;
    private int clienteId;

    public void setDadosUsuario(String cpf, String nomeUsuario, int clienteId) {
    this.cpf = cpf;
    this.nomeUsuario = nomeUsuario;
    this.clienteId = clienteId;
    }

    public void setDadosPedido(int pedidoId, String dataPedido, List<CarrinhoProduto> itensPedido, double totalPedido) {
        this.pedidoId = pedidoId;
        this.itensPedido = itensPedido;
        this.dataPedido = dataPedido;
        this.totalPedido = totalPedido;

        mostrarLoadingComDelay();
    }

    private void mostrarLoadingComDelay() {
  
        progressIndicator.setVisible(true);
        dadosPedidoContainer.setVisible(false);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> mostrarDadosPedido());
        pause.play();
    }

    private void mostrarDadosPedido() {
        // Esconder spinner e mostrar dados
        progressIndicator.setVisible(false);
        dadosPedidoContainer.setVisible(true);

        lblNumeroPedido.setText("Número do Pedido: " + pedidoId);
        lblDataPedido.setText("Data do Pedido: " + dataPedido);

        // Configurar colunas
        colProduto.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantidade()).asObject());
        colPreco.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrecoUnitario()).asObject());
        colSubtotal.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrecoUnitario() * data.getValue().getQuantidade()).asObject());

        tabelaProdutos.setItems(FXCollections.observableArrayList(itensPedido));

        lblTotalPedido.setText(String.format("Total: R$ %.2f", totalPedido));
    }

    @FXML
private void voltarALoja() {
    Stage stage = (Stage) dadosPedidoContainer.getScene().getWindow();
    SceneController.trocarCenaComController(stage, "/view/VerProdutoView.fxml", "Ver Produtos", (VerProdutoController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
    });
}

@FXML
private void verMeusPedidos() {
    Stage stage = (Stage) dadosPedidoContainer.getScene().getWindow();
    SceneController.trocarCenaComController(stage, "/view/PedidosView.fxml", "Meus Pedidos", (MeusPedidosController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
    });
}

}
