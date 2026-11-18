package com.gustavointech.compras.produto;

// Um produto do usuário; categoriaId e categoria são null quando ele está "sem categoria"
public class ProdutoResponse {

    private final Long id;
    private final String nome;
    private final Long categoriaId;
    private final String categoria;

    public ProdutoResponse(Long id, String nome, Long categoriaId, String categoria) {
        this.id = id;
        this.nome = nome;
        this.categoriaId = categoriaId;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoria() {
        return categoria;
    }
}