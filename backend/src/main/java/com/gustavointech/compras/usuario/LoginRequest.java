package com.gustavointech.compras.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// O que o cliente envia no login
public class LoginRequest {

    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 64)
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}