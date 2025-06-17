package com.hortifacil.controller;

import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;
import com.hortifacil.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class MeusPedidosController {

    @FXML
    private TableView<Pedido> tablePedidos;

    @FXML
    private TableColumn<Pedido, Integer> colNumeroPedido;

    @FXML
    private TableColumn<Pedido, String> colDataPedido;

    @FXML
    private TableColumn<Pedido, Double> colValorTotal;

    @FXML
    private TableColumn<Pedido, String> colStatus;

    @FXML
    private Button btnVoltar;

    private PedidoService pedidoService;
    private int clienteId;
    private String cpf;
    private String nomeUsuario;

    public void setDadosUsuario(String cpf, String nomeUsuario, int clienteId) {
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.clienteId = clienteId;
            carregarPedidos();
    }


    @FXML
    public void initialize() {
        pedidoService = PedidoService.getInstance();

        colNumeroPedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colDataPedido.setCellValueFactory(new PropertyValueFactory<>("dataPedido"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    setStyle("-fx-text-fill: green;");
                }
            }
        });

        tablePedidos.setRowFactory(tv -> {
            TableRow<Pedido> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Pedido pedido = row.getItem();
                    mostrarDetalhesPedido(pedido);
                }
            });
            return row;
        });
    }

    private void carregarPedidos() {
        List<Pedido> pedidos = pedidoService.listarPedidosPorCliente(clienteId);
        tablePedidos.setItems(FXCollections.observableArrayList(pedidos));
    }

    private void mostrarDetalhesPedido(Pedido pedido) {
        List<CarrinhoProduto> itens = pedidoService.buscarItensPedido(pedido.getIdPedido());

        String detalhes = itens.stream()
                .map(i -> i.getProduto().getNome() + " x" + i.getQuantidade())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("Nenhum item");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pedido #" + pedido.getIdPedido());
        alert.setHeaderText("Detalhes do Pedido");
        alert.setContentText(detalhes);
        alert.showAndWait();
    }

@FXML
private void handleVoltar() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();

    SceneController.<HomeClienteController>trocarCenaComDados(
        stage,
        "/view/HomeClienteView.fxml",
        "Home Cliente",
        controller -> controller.setDadosUsuario(cpf, nomeUsuario, clienteId)
    );
}

}
