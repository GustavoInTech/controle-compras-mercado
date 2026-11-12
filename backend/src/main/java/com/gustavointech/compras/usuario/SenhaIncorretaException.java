package com.gustavointech.compras.usuario;

public class SenhaIncorretaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SenhaIncorretaException() {
        super("Senha incorreta.");
    }
}