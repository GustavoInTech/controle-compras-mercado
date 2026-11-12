package com.gustavointech.compras.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A confirmação para excluir a conta: a senha atual
public class ExcluirContaRequest {

    @NotBlank
    @Size(max = 64)
    private String senha;

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}