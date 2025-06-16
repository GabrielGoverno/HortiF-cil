package com.hortifacil.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import com.hortifacil.model.Pedido;
import com.hortifacil.service.PedidoService;
import com.hortifacil.service.ClienteService;


import java.time.LocalDate;
import java.util.List;

public class ListaPedidosController {

    @FXML
    private TableView<Pedido> tablePedidos;

    @FXML
    private TableColumn<Pedido, Integer> colNumeroPedido;

    @FXML
    private TableColumn<Pedido, LocalDate> colDataPedido;

    @FXML
    private TableColumn<Pedido, Double> colValorTotal;

    @FXML
    private TableColumn<Pedido, String> colStatus;

    @FXML
    private TableColumn<Pedido, Void> colAcoes;

    @FXML
    private Button btnVoltarHome;

    private PedidoService pedidoService;
    private ClienteService clienteService;

    @FXML
    public void initialize() {
        pedidoService = PedidoService.getInstance();
        clienteService = ClienteService.getInstance();

        colNumeroPedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colDataPedido.setCellValueFactory(new PropertyValueFactory<>("dataPedido"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<Pedido, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    setText(status);
                    setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
                    setOnMouseClicked(e -> {
                        Pedido pedido = getTableView().getItems().get(getIndex());
                        mostrarDetalhesPedido(pedido);
                    });
                }
            }
        });

        adicionarBotoesAcoes();

        carregarPedidos();
    }

    private void adicionarBotoesAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btnFinalizar = new Button("Finalizar");
            private final Button btnDetalhes = new Button("Detalhes");
            private final HBox box = new HBox(5, btnDetalhes, btnFinalizar);

            {
                btnFinalizar.setOnAction(event -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    finalizarPedido(pedido);
                });

                btnDetalhes.setOnAction(event -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    mostrarDetalhesPedido(pedido);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    btnFinalizar.setDisable(pedido.getStatus().equalsIgnoreCase("FINALIZADO"));
                    setGraphic(box);
                }
            }
        });
    }

    private void finalizarPedido(Pedido pedido) {
        pedido.setStatus("FINALIZADO");
        boolean sucesso = pedidoService.atualizarStatusPedido(pedido);
        if (sucesso) {
            carregarPedidos();
        } else {
            System.out.println("Erro ao finalizar pedido.");
            // Aqui você pode exibir um alerta para o usuário
        }
    }

    private void mostrarDetalhesPedido(Pedido pedido) {
        String detalhes = pedido.getItens().stream()
                .map(i -> i.getProduto().getNome() + " x" + i.getQuantidade())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("Nenhum item");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhes do Pedido #" + pedido.getIdPedido());
        alert.setHeaderText("Itens do Pedido");
        alert.setContentText(detalhes);
        alert.showAndWait();
    }

    private void carregarPedidos() {
        List<Pedido> pedidos = pedidoService.listarTodosPedidos();
        tablePedidos.getItems().setAll(pedidos);
    }
    
 @FXML
private void btnVoltarHome() {
    Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
    SceneController.trocarCena(stage, "/view/HomeAdminView.fxml", "home");
}

}