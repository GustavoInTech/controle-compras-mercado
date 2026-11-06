package com.gustavointech.compras.usuario;

// O que o sistema devolve: só o que é seguro mostrar (nunca a senha nem o hash)
public class UsuarioResponse {

    private final Long id;
    private final String email;

    public UsuarioResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}