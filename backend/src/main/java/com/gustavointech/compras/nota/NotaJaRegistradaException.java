package com.gustavointech.compras.nota;

public class NotaJaRegistradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotaJaRegistradaException() {
        super("Você já registrou esta nota.");
    }
}