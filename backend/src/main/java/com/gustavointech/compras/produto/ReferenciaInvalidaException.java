package com.gustavointech.compras.produto;

// O pedido aponta para algo que não existe (categoria ou mercado)
public class ReferenciaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReferenciaInvalidaException(String mensagem) {
        super(mensagem);
    }
}