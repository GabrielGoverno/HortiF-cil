package com.hortifacil.controller;

import com.hortifacil.dao.*;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.EstoqueService;
import com.hortifacil.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class CarrinhoProdutoController {

    @FXML
    private ListView<CarrinhoProduto> lvCarrinho;

    @FXML
    private Button btnFinalizar;

    @FXML
    private Button btnRemoverSelecionado;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnVoltar;

    private ObservableList<CarrinhoProduto> carrinho = FXCollections.observableArrayList();

    private String cpf;
    private String nomeUsuario;
    private int clienteId;

    private EstoqueService estoqueService;
    private PedidoService pedidoService;
    private CarrinhoProdutoDAO carrinhoProdutoDAO;

    @FXML
    public void initialize() {
        try {
            var conn = DatabaseConnection.getConnection();
            var produtoDAO = new ProdutoDAOImpl(conn);
            var produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn);
            var pedidoDAO = new PedidoDAO();

            btnVoltar.setOnAction(e -> voltarParaVerProdutos());

            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);
            pedidoService = PedidoService.getInstance();

            carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao conectar ao banco: " + e.getMessage());
        }

        btnRemoverSelecionado.setOnAction(e -> removerItemSelecionado());
        btnFinalizar.setOnAction(e -> finalizarPedido());
        lvCarrinho.setItems(carrinho);

        lvCarrinho.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CarrinhoProduto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getProduto().getNome() + " - Qtde: " + item.getQuantidade() + " - R$ " + item.getPrecoUnitario());
                }
            }
        });

        atualizarTotal();
    }

    public void adicionarAoCarrinho(String nomeProduto, int quantidade) {
        if (quantidade <= 0) return;

        for (CarrinhoProduto item : carrinho) {
            if (item.getProduto().getNome().equalsIgnoreCase(nomeProduto)) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                lvCarrinho.refresh();
                atualizarTotal();
                lblStatus.setText("Quantidade atualizada no carrinho.");
                carrinhoProdutoDAO.atualizarQuantidade(item.getClienteId(), item.getProduto().getId(), item.getQuantidade());
                return;
            }
        }

        ProdutoEstoque produtoEstoque = estoqueService.buscarProdutoEstoquePorNome(nomeProduto);
        if (produtoEstoque != null) {
            Produto produto = produtoEstoque.getProduto();

            CarrinhoProduto novoItem = new CarrinhoProduto(
                clienteId,
                produto,
                quantidade,
                produto.getPreco()
            );

            carrinho.add(novoItem);
            lvCarrinho.refresh();
            atualizarTotal();
            lblStatus.setText("Produto adicionado ao carrinho.");

            carrinhoProdutoDAO.adicionarAoCarrinho(novoItem);
        } else {
            lblStatus.setText("Produto não encontrado no estoque.");
        }
    }

    private void atualizarTotal() {
        double total = 0.0;
        for (CarrinhoProduto item : carrinho) {
            total += item.getPrecoUnitario() * item.getQuantidade();
        }
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void removerItemSelecionado() {
        CarrinhoProduto itemSelecionado = lvCarrinho.getSelectionModel().getSelectedItem();
        if (itemSelecionado != null) {
            carrinho.remove(itemSelecionado);
            carrinhoProdutoDAO.removerItem(clienteId, itemSelecionado.getProduto().getId());
            atualizarTotal();
            lblStatus.setText("Item removido do carrinho.");
        } else {
            lblStatus.setText("Nenhum item selecionado.");
        }
    }

    public void setDadosUsuario(String cpf, String nomeUsuario, int clienteId) {
    this.cpf = cpf;
    this.nomeUsuario = nomeUsuario;
    this.clienteId = clienteId;

    // Recarrega os itens corretos do cliente atual
    List<CarrinhoProduto> itensPersistidos = carrinhoProdutoDAO.listarPorCliente(clienteId);
    carrinho.setAll(itensPersistidos);
    atualizarTotal();
}


    private boolean verificarEstoqueParaCarrinho(List<CarrinhoProduto> carrinho) {
        for (CarrinhoProduto item : carrinho) {
            boolean disponivel = estoqueService.verificarEstoque(item.getProduto().getNome(), item.getQuantidade());
            if (!disponivel) {
                return false;
            }
        }
        return true;
    }

    private void finalizarPedido() {
    if (carrinho.isEmpty()) {
        lblStatus.setText("Carrinho vazio.");
        return;
    }

    boolean temEstoque = verificarEstoqueParaCarrinho(carrinho);
    if (!temEstoque) {
        lblStatus.setText("Estoque insuficiente para algum produto.");
        return;
    }

    boolean sucessoEstoque = estoqueService.removerProdutosDoEstoque(carrinho);
    if (!sucessoEstoque) {
        lblStatus.setText("Erro ao atualizar estoque.");
        return;
    }

    boolean pedidoCriado = pedidoService.criarPedido(clienteId, carrinho);
    if (!pedidoCriado) {
        lblStatus.setText("Erro ao criar pedido.");
        return;
    }

    carrinho.clear();
    carrinhoProdutoDAO.limparCarrinhoDoCliente(clienteId);
    atualizarTotal();

    lblStatus.setText("Pedido finalizado com sucesso!");
    btnFinalizar.setDisable(true);

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Confirmação");
    alert.setHeaderText(null);
    alert.setContentText("Pedido finalizado com sucesso!");
    alert.showAndWait();
}

      @FXML
    private void voltarParaVerProdutos() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();
    SceneController.trocarCenaComController(stage, "/view/VerProdutoView.fxml", "Ver Produtos", (VerProdutoController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
    });
}

}
