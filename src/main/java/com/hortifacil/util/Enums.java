package com.hortifacil.util;

public class Enums {

    public enum TipoUsuario {
        ADMIN,
        CLIENTE
    }

    public enum StatusUsuario {
        ATIVO,
        INATIVO
    }

    public enum ResultadoLogin {
        SUCESSO,
        USUARIO_NAO_ENCONTRADO,
        SENHA_INVALIDA,
        USUARIO_INATIVO
    }

    public enum StatusAssinatura {
        ATIVA,
        CANCELADA,
        EXPIRADA
    }

    public enum TipoPlano {
        MENSAL,
        TRIMESTRAL,
        ANUAL
    }

    public enum StatusPagamento {
        APROVADO,
        PENDENTE,
        RECUSADO
    }

    public enum StatusCarrinho {
        CRIADO,
        ABERTO,
        FINALIZADO,
        ABANDONADO
    }

    public enum StatusPedido {
        EM_ANDAMENTO,
        CANCELADO,
        FINALIZADO
    }

    public enum TipoRelato {
        ERRO,
        SUGESTAO
    }
}
