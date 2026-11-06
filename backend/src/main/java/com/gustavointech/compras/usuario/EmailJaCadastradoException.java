package com.gustavointech.compras.usuario;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Já existe uma conta com este e-mail.");
    }
}