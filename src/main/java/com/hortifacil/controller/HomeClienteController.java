package com.hortifacil.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;

public class HomeClienteController {

    @FXML
    private Label welcomeLabel;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;

    public void setDadosUsuario(String cpf, String nomeUsuario, int clienteId) {
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.clienteId = clienteId;

        if (welcomeLabel != null && nomeUsuario != null) {
            welcomeLabel.setText("Bem-vindo, " + nomeUsuario + "!");
        }
    }

    @FXML
    private void handleVerProdutos(ActionEvent event) {
        if (cpf == null || nomeUsuario == null) {
            System.err.println("Dados do usuário não foram inicializados corretamente.");
            return;
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneController.<VerProdutoController>trocarCenaComDados(
            stage,
            "/view/VerProdutoView.fxml",
            "Produtos Disponíveis",
            controller -> controller.setDadosUsuario(cpf, nomeUsuario, clienteId)
        );
    }

    @FXML
private void handleVerPedidos() {
    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    SceneController.trocarCenaComController(stage, "/view/MeusPedidosView.fxml", "Meus Pedidos", (MeusPedidosController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
    });
}


    @FXML
    private void handleSair(ActionEvent event) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/LoginView.fxml", "Login");
    }
}
