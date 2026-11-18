package com.gustavointech.compras.produto;

import java.time.LocalDate;

// Um nome de item, numa loja, que ainda não foi ligado a nenhum produto (uma linha da tela "para revisar")
public class PendenteResponse {

    private final Long mercadoId;
    private final String mercado;
    private final String nomeNormalizado;
    private final String nomeOriginal;
    private final String unidade;
    private final long ocorrencias;
    private final LocalDate ultimaCompra;

    public PendenteResponse(Long mercadoId, String mercado, String nomeNormalizado, String nomeOriginal,
            String unidade, long ocorrencias, LocalDate ultimaCompra) {
        this.mercadoId = mercadoId;
        this.mercado = mercado;
        this.nomeNormalizado = nomeNormalizado;
        this.nomeOriginal = nomeOriginal;
        this.unidade = unidade;
        this.ocorrencias = ocorrencias;
        this.ultimaCompra = ultimaCompra;
    }

    public Long getMercadoId() {
        return mercadoId;
    }

    public String getMercado() {
        return mercado;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getUnidade() {
        return unidade;
    }

    public long getOcorrencias() {
        return ocorrencias;
    }

    public LocalDate getUltimaCompra() {
        return ultimaCompra;
    }
}