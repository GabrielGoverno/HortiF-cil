package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import com.hortifacil.model.ItemPedido;
import com.hortifacil.model.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DetalhesPedidoAdminController {

    @FXML private Label lblNomeCliente;
    @FXML private Label lblEnderecoCliente;
    @FXML private TableView<ItemPedido> tableItensPedido;
    @FXML private TableColumn<ItemPedido, String> colProduto;
    @FXML private TableColumn<ItemPedido, Integer> colQuantidade;
    @FXML private TableColumn<ItemPedido, Double> colPrecoUnitario;
    @FXML private TableColumn<ItemPedido, Double> colSubtotal;
    @FXML private Button btnVoltar;


    private final ObservableList<ItemPedido> listaItens = FXCollections.observableArrayList();

    private int idPedido;

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
        carregarDados();
    }

    @FXML
    public void initialize() {
    colProduto.setCellValueFactory(cellData -> cellData.getValue().nomeProdutoProperty());
    colQuantidade.setCellValueFactory(cellData -> cellData.getValue().quantidadeProperty().asObject());
    colPrecoUnitario.setCellValueFactory(cellData -> cellData.getValue().precoUnitarioProperty().asObject());
    colSubtotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty().asObject());
}



public void setPedido(Pedido pedido) {
    setIdPedido(pedido.getIdPedido());
}


    private void carregarDados() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String sqlInfo = """
                SELECT c.nome AS nome_cliente, 
                       CONCAT(e.rua, ', ', e.numero, ' - ', e.bairro) AS endereco
                FROM pedido p
                JOIN cliente c ON p.id_cliente = c.id_cliente
                LEFT JOIN endereco e ON e.id_cliente = c.id_cliente
                WHERE p.id_pedido = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlInfo)) {
                stmt.setInt(1, idPedido);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblNomeCliente.setText("Cliente: " + rs.getString("nome_cliente"));
                    lblEnderecoCliente.setText("Endereço: " + rs.getString("endereco"));
                }
            }

            String sqlItens = """
                SELECT pr.nome, pp.quantidade, pp.preco_unitario
                FROM pedido_produto pp
                JOIN produto pr ON pr.id_produto = pp.id_produto
                WHERE pp.id_pedido = ?
            """;

            listaItens.clear();
            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {
                stmt.setInt(1, idPedido);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    int qtd = rs.getInt("quantidade");
                    double preco = rs.getDouble("preco_unitario");
                    listaItens.add(new ItemPedido(nome, qtd, preco));
                }
            }

            tableItensPedido.setItems(listaItens);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
private void handleVoltar() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();
    SceneController.trocarCena(stage, "/view/ListaPedidosView.fxml", "Lista de Pedidos");
}

}