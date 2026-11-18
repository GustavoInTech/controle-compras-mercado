package com.gustavointech.compras.produto;

// O resultado de ligar um nome a um produto: quantos itens (de todas as notas) foram ligados
public class AssociacaoResponse {

    private final Long produtoId;
    private final int itensLigados;

    public AssociacaoResponse(Long produtoId, int itensLigados) {
        this.produtoId = produtoId;
        this.itensLigados = itensLigados;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public int getItensLigados() {
        return itensLigados;
    }
}