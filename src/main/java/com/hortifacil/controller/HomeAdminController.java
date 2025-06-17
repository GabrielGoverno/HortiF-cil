package com.hortifacil.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.io.IOException;

import javafx.event.ActionEvent;

public class HomeAdminController {

    @FXML
    private Button btnAdicionarEstoque;
    
    @FXML
    private Button btnCadastroProdutos;

    @FXML
    private Button btnSair;

    @FXML
    private Button btnAbrirTelaPedidos;

     @FXML
    private Button btnEstoqueAtual;

    @FXML
    private void abrirAdicionarEstoque(ActionEvent event) {
        SceneController.trocarCena(
            (Stage) btnAdicionarEstoque.getScene().getWindow(),
            "/view/AdicionarEstoqueView.fxml",  
            "Controle de Estoque"
        );
    }

    @FXML
    private void abrirEstoqueAtual() {
        Stage stage = (Stage) btnEstoqueAtual.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/EstoqueResumoView.fxml", "Estoque Atual");
    }


    @FXML
    private void abrirCadastroProdutos(ActionEvent event) {
        SceneController.trocarCena(
            (Stage) btnCadastroProdutos.getScene().getWindow(),
            "/view/CadastroProdutoView.fxml",  // Ajuste o caminho se necessário
            "Cadastro de Produtos"
        );
    }

    @FXML
    private void voltarLogin() {
        Stage stage = (Stage) btnSair.getScene().getWindow();
        SceneController.trocarCena(stage, "/view/LoginView.fxml", "Login");
    }

    @FXML
private Button btnVerPedidos;

@FXML
private void abrirTelaPedidos() {
    try {
        // Pega a janela atual a partir de um componente da cena atual, por exemplo, o próprio botão que chama esse método
        Stage stage = (Stage) btnAbrirTelaPedidos.getScene().getWindow(); // btnAbrirTelaPedidos é o botão que chama essa função

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ListaPedidosView.fxml"));
        Parent root = loader.load();

        stage.setTitle("Lista de Pedidos");
        stage.setScene(new Scene(root));
        stage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
