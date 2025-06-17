package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.UnidadeMedida;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdutoEstoqueDAOImpl implements ProdutoEstoqueDAO {

    private final Connection connection;
    private final ProdutoDAO produtoDAO; 

    public ProdutoEstoqueDAOImpl(Connection connection, ProdutoDAO produtoDAO) {
        this.connection = connection;
        this.produtoDAO = produtoDAO;  
    }


    @Override
    public int adicionarLote(ProdutoEstoque lote) {
        String sql = "INSERT INTO produto_estoque (id_produto, quantidade, data_colheita, data_validade) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, lote.getProduto().getId());
            stmt.setInt(2, lote.getQuantidade());
           if (lote.getDataColhido() != null) {
                stmt.setDate(3, Date.valueOf(lote.getDataColhido()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (lote.getDataValidade() != null) {
                stmt.setDate(4, Date.valueOf(lote.getDataValidade()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao adicionar lote de estoque, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao obter ID do lote inserido.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
public List<ProdutoEstoque> listarLotesPorProduto(int produtoId) {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
        SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade,
               p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
               u.id_unidade, u.nome AS nome_unidade
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        JOIN unidade_medida u ON p.id_unidade = u.id_unidade
        WHERE pe.id_produto = ?
        ORDER BY pe.data_colheita ASC  -- FIFO: mais antigo primeiro
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, produtoId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UnidadeMedida unidade = new UnidadeMedida(
                rs.getInt("id_unidade"),
                rs.getString("nome_unidade")
            );

            Produto produto = new Produto(
                produtoId,
                rs.getString("nome"),
                rs.getDouble("preco_unitario"),
                rs.getString("imagem_path"),
                rs.getString("descricao"),
                unidade
            );

            Date dataColheita = rs.getDate("data_colheita");
            Date dataValidade = rs.getDate("data_validade");

            ProdutoEstoque lote = new ProdutoEstoque(
                rs.getInt("id"),
                produto,
                rs.getInt("quantidade"),
                (dataColheita != null) ? dataColheita.toLocalDate() : null,
                (dataValidade != null) ? dataValidade.toLocalDate() : null
            );

            lotes.add(lote);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lotes;
}


    @Override
    public boolean atualizarQuantidadeLote(int idLote, int novaQuantidade) {
        String sql = "UPDATE produto_estoque SET quantidade = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, novaQuantidade);
            stmt.setInt(2, idLote);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removerLote(int idLote) {
        String sql = "DELETE FROM produto_estoque WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idLote);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean salvar(ProdutoEstoque produtoEstoque) {
        return adicionarLote(produtoEstoque) != -1;
    }

   @Override
public List<ProdutoEstoque> listarTodos() {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
                    SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade,
                        p.id_produto as id_produto, p.nome, p.preco_unitario as preco_unitario,
                        p.imagem_path as caminho_imagem, p.descricao,
                        u.id_unidade, u.nome as nome_unidade
                    FROM produto_estoque pe
                    JOIN produto p ON pe.id_produto = p.id_produto
                    JOIN unidade_medida u ON p.id_unidade = u.id_unidade
                    ORDER BY pe.data_validade ASC
                """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UnidadeMedida unidade = new UnidadeMedida(
                rs.getInt("id_unidade"),
                rs.getString("nome_unidade")
            );

            Produto produto = new Produto(
                rs.getInt("id_produto"),
                rs.getString("nome"),
                rs.getDouble("preco_unitario"),
                rs.getString("caminho_imagem"),
                rs.getString("descricao"),
                unidade
            );

            Date dataColheita = rs.getDate("data_colheita");
            Date dataValidade = rs.getDate("data_validade");

            ProdutoEstoque lote = new ProdutoEstoque(
                rs.getInt("id"),
                produto,
                rs.getInt("quantidade"),
                (dataColheita != null) ? dataColheita.toLocalDate() : null,
                (dataValidade != null) ? dataValidade.toLocalDate() : null
            );

            lotes.add(lote);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lotes;
}

   @Override
public List<ProdutoEstoque> buscarPorNomeProduto(String nomeProduto) throws SQLException {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
            SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade,
                   p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
                   u.id_unidade, u.nome AS nome_unidade
            FROM produto_estoque pe
            JOIN produto p ON pe.id_produto = p.id_produto
            JOIN unidade_medida u ON p.id_unidade = u.id_unidade
            WHERE p.nome LIKE ?
            ORDER BY pe.data_validade ASC
            """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, "%" + nomeProduto + "%");
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UnidadeMedida unidade = new UnidadeMedida(
                    rs.getInt("id_unidade"),
                    rs.getString("nome_unidade")
                );

                Produto produto = new Produto(
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getDouble("preco_unitario"),
                    rs.getString("imagem_path"),
                    rs.getString("descricao"),
                    unidade
                );

                Date dataColheita = rs.getDate("data_colheita");
                LocalDate localDataColheita = (dataColheita != null) ? dataColheita.toLocalDate() : null;

                Date dataValidade = rs.getDate("data_validade");
                LocalDate localDataValidade = (dataValidade != null) ? dataValidade.toLocalDate() : null;

                ProdutoEstoque lote = new ProdutoEstoque(
                    rs.getInt("id"),
                    produto,
                    rs.getInt("quantidade"),
                    localDataColheita,
                    localDataValidade
                );

                lotes.add(lote);
            }
        }
    }

    return lotes;
}

  @Override
    public ProdutoEstoque buscarPorProdutoData(int produtoId, LocalDate dataColhido, LocalDate dataValidade) throws SQLException {
        String sql = "SELECT * FROM produto_estoque WHERE id_produto = ? AND data_colheita = ? AND data_validade = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            stmt.setDate(2, java.sql.Date.valueOf(dataColhido));
            stmt.setDate(3, java.sql.Date.valueOf(dataValidade));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produto produto = produtoDAO.buscarPorId(produtoId);  // usa o produtoDAO injetado
                    return new ProdutoEstoque(
                        rs.getInt("id"),
                        produto,
                        rs.getInt("quantidade"),
                        rs.getDate("data_colheita").toLocalDate(),
                        rs.getDate("data_validade").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

public int getQuantidadeEstoquePorNome(String nomeProduto) throws SQLException {
    String sql = "SELECT SUM(pe.quantidade)\r\n" +
                "FROM produto_estoque pe\r\n" +
                "JOIN produto p ON pe.id_produto = p.id_produto\r\n" +
                "WHERE p.nome = ?\r\n" +
                "";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, nomeProduto);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }
}

@Override
public List<ProdutoEstoque> buscarLotesPorProdutoOrdenados(int idProduto) throws SQLException {
    String sql = "SELECT * FROM produto_estoque WHERE id_produto = ? ORDER BY data_colheita ASC";
    List<ProdutoEstoque> lotes = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = produtoDAO.buscarPorId(rs.getInt("id_produto"));
                ProdutoEstoque lote = new ProdutoEstoque(
                    rs.getInt("id"),
                    produto,
                    rs.getInt("quantidade"),
                    rs.getDate("data_colheita").toLocalDate(),
                    rs.getDate("data_validade").toLocalDate()
                );
                lotes.add(lote);
            }
        }
    }
    return lotes;
}

public int somarQuantidadePorProduto(int idProduto) throws SQLException {
   int total = 0;
    String sql = "SELECT SUM(quantidade) FROM produto_estoque WHERE id_produto = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                total = rs.getInt(1);
            }
        }
    }
    
    return total;
}

}