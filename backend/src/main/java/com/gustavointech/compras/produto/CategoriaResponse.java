package com.gustavointech.compras.produto;

public class CategoriaResponse {

    private final Long id;
    private final String nome;

    public CategoriaResponse(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}