package com.gustavointech.compras.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// O que o cliente envia para criar ou editar um produto
public class ProdutoRequest {

    @NotBlank
    @Size(max = 200)
    private String nome;

    // Opcional: sem categoria, o produto aparece como "Sem categoria"
    private Long categoriaId;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}