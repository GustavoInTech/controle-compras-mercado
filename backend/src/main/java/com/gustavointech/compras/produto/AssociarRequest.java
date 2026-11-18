package com.gustavointech.compras.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Ligar um nome (numa loja) a um produto que já existe
public class AssociarRequest {

    @NotNull
    private Long mercadoId;

    @NotBlank
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