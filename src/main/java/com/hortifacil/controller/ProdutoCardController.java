package com.hortifacil.controller;

import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;

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
    @FXML private Label quantidadeLabel;

    private int quantidadeDisponivel;
    private Produto produto;
    private ProdutoCardListener listener;

    public interface ProdutoCardListener {
        void onAdicionarAoCarrinho(Produto produto, int quantidade);
    }

    public void setListener(ProdutoCardListener listener) {
        this.listener = listener;
    }

   public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
    this.produto = produtoEstoque.getProduto();
    this.quantidadeDisponivel = produtoEstoque.getQuantidade();

    nomeLabel.setText(produto.getNome());
    quantidadeLabel.setText(quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis");
    precoLabel.setText(String.format("R$ %.2f por %s", produto.getPreco(), produto.getUnidade()));

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

    Rectangle clip = new Rectangle(140, 140);
    imagemView.setClip(clip);

    adicionarBtn.setDisable(false);

    adicionarBtn.setOnAction(e -> handleAdicionarAoCarrinho());
}

public void setProdutoQuantidadeDisponivel(Produto produto, int quantidadeDisponivel) {
    this.produto = produto;
    this.quantidadeDisponivel = quantidadeDisponivel;

    nomeLabel.setText(produto.getNome());
    quantidadeLabel.setText(quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis");
    precoLabel.setText(String.format("R$ %.2f por %s", produto.getPreco(), produto.getUnidade()));

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

    Rectangle clip = new Rectangle(140, 140);
    imagemView.setClip(clip);

    adicionarBtn.setDisable(false);
    adicionarBtn.setOnAction(e -> handleAdicionarAoCarrinho());
}

    @FXML
    private void handleAdicionarAoCarrinho() {
        int quantidade = 1;
            try {
                quantidade = Integer.parseInt(quantidadeField.getText());
                if (quantidade <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                mensagemLabel.setText("Quantidade inválida. Usando 1.");
                quantidade = 1;
            }

            if (quantidade > quantidadeDisponivel) {
                mensagemLabel.setText("❌ Só temos " + quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis.");
                return;
            }

            if (listener != null) {
            listener.onAdicionarAoCarrinho(produto, quantidade);
            mensagemLabel.setText("✅ Adicionado ao carrinho!");
        }
    }
}
