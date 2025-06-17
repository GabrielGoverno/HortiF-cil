package com.hortifacil.controller;

import com.hortifacil.dao.*;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;

import com.hortifacil.service.EstoqueService;
import com.hortifacil.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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
    private ProdutoEstoqueDAO produtoEstoqueDAO;

    @FXML
    public void initialize() {
        try {
            var conn = DatabaseConnection.getConnection();
            var produtoDAO = new ProdutoDAOImpl(conn); 
           this.produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, produtoDAO);

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

    public boolean verificarEstoque(String nomeProduto, int quantidadeSolicitada) throws SQLException {
    int quantidadeDisponivel = produtoEstoqueDAO.getQuantidadeEstoquePorNome(nomeProduto);
    
    return quantidadeDisponivel >= quantidadeSolicitada;
}


   public void adicionarAoCarrinho(String nomeProduto, int quantidade) {
    System.out.println("clienteId recebido no adicionarAoCarrinho: " + clienteId);
    if (quantidade <= 0) return;

    for (CarrinhoProduto item : carrinho) {
        if (item.getProduto().getNome().equalsIgnoreCase(nomeProduto)) {
            int novaQuantidade = item.getQuantidade() + quantidade;

            try {
                boolean estoqueDisponivel = estoqueService.verificarEstoque(nomeProduto, novaQuantidade);
                if (!estoqueDisponivel) {
                    lblStatus.setText("Quantidade total no carrinho ultrapassa o estoque disponível.");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                lblStatus.setText("Erro ao verificar estoque.");
                return;
            }

            item.setQuantidade(novaQuantidade);
            lvCarrinho.refresh();
            atualizarTotal();
            lblStatus.setText("Quantidade atualizada no carrinho.");
            
            try {
                carrinhoProdutoDAO.atualizarQuantidade(item.getClienteId(), item.getProduto().getId(), novaQuantidade);
            } catch (SQLException e) {
                e.printStackTrace();
                lblStatus.setText("Erro ao atualizar quantidade no banco.");
            }
            return;
        }
    }

    // Produto não está no carrinho, verifica estoque com a quantidade solicitada
    try {
        boolean estoqueDisponivel = estoqueService.verificarEstoque(nomeProduto, quantidade);
        if (!estoqueDisponivel) {
            lblStatus.setText("Quantidade solicitada ultrapassa o estoque disponível.");
            return;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro ao acessar o estoque.");
        return;
    }

    try {
        Produto produto = estoqueService.buscarProdutoPorNome(nomeProduto);

        if (produto != null) {
            CarrinhoProduto novoItem = new CarrinhoProduto(
                clienteId,
                produto,
                quantidade,
                produto.getPreco()
            );

          carrinhoProdutoDAO.adicionarAoCarrinho(novoItem);

        List<CarrinhoProduto> itensAtualizados = carrinhoProdutoDAO.listarPorCliente(clienteId);
        carrinho.setAll(itensAtualizados);
        lvCarrinho.refresh();
        atualizarTotal();
        lblStatus.setText("Produto adicionado ao carrinho.");

        } else {
            lblStatus.setText("Produto não encontrado no estoque.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro ao acessar o estoque.");
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
        try {
            carrinhoProdutoDAO.removerItem(clienteId, itemSelecionado.getProduto().getId());
        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao remover item do banco.");
            return;
        }
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

    try {
        List<CarrinhoProduto> itensPersistidos = carrinhoProdutoDAO.listarPorCliente(clienteId);
        carrinho.setAll(itensPersistidos);
        atualizarTotal();
    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro ao carregar itens do carrinho.");
    }
}

    private boolean verificarEstoqueParaCarrinho(List<CarrinhoProduto> itens) {
    try {
        for (CarrinhoProduto item : itens) {
            boolean disponivel = estoqueService.verificarEstoque(item.getProduto().getNome(), item.getQuantidade());
            if (!disponivel) {
                return false;
            }
        }
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro ao verificar o estoque.");
        return false;
    }
}

 
private void finalizarPedido() {
    if (carrinho.isEmpty()) {
        lblStatus.setText("Carrinho vazio.");
        return;
    }

    try (var conn = DatabaseConnection.getConnection()) {
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

        int pedidoIdCriado = pedidoService.criarPedidoRetornaId(conn, clienteId, List.copyOf(carrinho));
        if (pedidoIdCriado <= 0) {
            lblStatus.setText("Erro ao criar pedido.");
            return;
        }

        double total = carrinho.stream().mapToDouble(i -> i.getPrecoUnitario() * i.getQuantidade()).sum();
        String dataPedido = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Limpa carrinho e banco
        carrinho.clear();
        carrinhoProdutoDAO.limparCarrinhoDoCliente(clienteId);
        atualizarTotal();

        lblStatus.setText("Pedido finalizado com sucesso!");
        btnFinalizar.setDisable(true);

        List<CarrinhoProduto> itensPedido;
        try {
            itensPedido = carrinhoProdutoDAO.listarPorPedido(pedidoIdCriado);
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblStatus.setText("Erro ao carregar itens do pedido.");
            return;
        }

        Stage stage = (Stage) btnFinalizar.getScene().getWindow();
        SceneController.trocarCenaComController(stage, "/view/PedidoFinalizadoView.fxml", "Pedido Finalizado", (PedidoFinalizadoController controller) -> {
            controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
            controller.setDadosPedido(pedidoIdCriado, dataPedido, itensPedido, total);
        });

    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro de banco de dados ao finalizar pedido.");
    }
}

     @FXML
    private void voltarParaVerProdutos() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();
    SceneController.trocarCenaComController(stage, "/view/VerProdutoView.fxml", "Ver Produtos", (VerProdutoController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, clienteId);
    });
}

}