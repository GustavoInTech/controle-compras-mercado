package com.gustavointech.compras.produto;

// A categoria sugerida e de onde veio a sugestão: MESMO_NOME (o mesmo nome já confirmado em outro mercado),
// DICIONARIO (palavras conhecidas no nome) ou PADRAO (sem pista, sugere "Outros").
public class SugestaoCategoriaResponse {

    private final Long categoriaId;
    private final String nome;
    private final String origem;

    public SugestaoCategoriaResponse(Long categoriaId, String nome, String origem) {
        this.categoriaId = categoriaId;
        this.nome = nome;
        this.origem = origem;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getNome() {
        return nome;
    }

    public String getOrigem() {
        return origem;
    }
}