package com.hortifacil.controller;

import com.hortifacil.model.Produto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class ProdutoCardController {

    @FXML private Label nomeLabel;
    @FXML private Label precoLabel;
    @FXML private ImageView imagemView;
    @FXML private Button adicionarBtn;
    @FXML private Label mensagemLabel;
    @FXML private TextField quantidadeField;

    private Produto produto;
    private ProdutoCardListener listener;

    public interface ProdutoCardListener {
        void onAdicionarAoCarrinho(Produto produto, int quantidade);
    }

    public void setListener(ProdutoCardListener listener) {
        this.listener = listener;
    }

    public void setProduto(Produto produto) {
    this.produto = produto;

    nomeLabel.setText(produto.getNome());
    precoLabel.setText(String.format("R$ %.2f", produto.getPreco()));
    mensagemLabel.setText(""); // Limpa feedback anterior

    // Carrega imagem do produto
    var recurso = getClass().getResource(produto.getCaminhoImagem());
    if (recurso != null) {
        Image img = new Image(recurso.toExternalForm());
        imagemView.setImage(img);
    } else {
        imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
    }

    imagemView.setFitWidth(140);
    imagemView.setFitHeight(140);
    imagemView.setPreserveRatio(true);
    imagemView.setSmooth(true);
    imagemView.setCache(true);

    // Clip fixo para evitar distorção
    Rectangle clip = new Rectangle(140, 140);
    imagemView.setClip(clip);

    adicionarBtn.setDisable(false); // garante que botão esteja habilitado

    // CORRETO: define uma única vez a ação do botão
    adicionarBtn.setOnAction(e -> handleAdicionarAoCarrinho());
}

    @FXML
private void handleAdicionarAoCarrinho() {
    int quantidade = 1;
    try {
        quantidade = Integer.parseInt(quantidadeField.getText());
        if (quantidade <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        System.out.println("Quantidade inválida. Usando 1.");
        quantidade = 1;
    }

    if (listener != null) {
        listener.onAdicionarAoCarrinho(produto, quantidade);
        mensagemLabel.setText("Adicionado!");
    }
}

}
