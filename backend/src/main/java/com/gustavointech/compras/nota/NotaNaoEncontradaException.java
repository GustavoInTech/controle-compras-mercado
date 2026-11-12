package com.gustavointech.compras.nota;

public class NotaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotaNaoEncontradaException() {
        super("Nota não encontrada.");
    }
}