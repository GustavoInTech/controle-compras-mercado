package com.gustavointech.compras.produto;

import jakarta.validation.constraints.Size;

// Criar um produto e, de uma vez, ligar a ele um nome que estava "para revisar".
// Os dois campos do apelido vão juntos ou nenhum deles.
public class CriarProdutoRequest extends ProdutoRequest {

    private Long mercadoId;

    @Size(max = 300)
    private String nomeNormalizado;

    public Long getMercadoId() {
        return mercadoId;
    }

    public void setMercadoId(Long mercadoId) {
        this.mercadoId = mercadoId;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public void setNomeNormalizado(String nomeNormalizado) {
        this.nomeNormalizado = nomeNormalizado;
    }
}