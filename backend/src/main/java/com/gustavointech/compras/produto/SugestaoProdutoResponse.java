package com.gustavointech.compras.produto;

// "Parece com este produto que você já confirmou". A confiança é ALTA quando o nome é exatamente o mesmo
// (confirmado em outro mercado) e MEDIA quando é só parecido.
public class SugestaoProdutoResponse {

    private final Long produtoId;
    private final String nome;
    private final String confianca;

    public SugestaoProdutoResponse(Long produtoId, String nome, String confianca) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.confianca = confianca;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public String getConfianca() {
        return confianca;
    }
}