package com.gustavointech.compras.produto;

import java.time.LocalDate;

// Um nome de item, numa loja, que ainda não foi ligado a nenhum produto (uma linha da tela "para revisar"),
// já com as sugestões. O sistema sugere, mas nunca aplica sozinho.
public class PendenteResponse {

    private final Long mercadoId;
    private final String mercado;
    private final String nomeNormalizado;
    private final String nomeOriginal;
    private final String unidade;
    private final long ocorrencias;
    private final LocalDate ultimaCompra;
    private final SugestaoProdutoResponse sugestaoProduto;
    private final SugestaoCategoriaResponse sugestaoCategoria;

    public PendenteResponse(Long mercadoId, String mercado, String nomeNormalizado, String nomeOriginal,
            String unidade, long ocorrencias, LocalDate ultimaCompra,
            SugestaoProdutoResponse sugestaoProduto, SugestaoCategoriaResponse sugestaoCategoria) {
        this.mercadoId = mercadoId;
        this.mercado = mercado;
        this.nomeNormalizado = nomeNormalizado;
        this.nomeOriginal = nomeOriginal;
        this.unidade = unidade;
        this.ocorrencias = ocorrencias;
        this.ultimaCompra = ultimaCompra;
        this.sugestaoProduto = sugestaoProduto;
        this.sugestaoCategoria = sugestaoCategoria;
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

    // null quando não há nenhum produto parecido entre os que o usuário já
    // confirmou
    public SugestaoProdutoResponse getSugestaoProduto() {
        return sugestaoProduto;
    }

    public SugestaoCategoriaResponse getSugestaoCategoria() {
        return sugestaoCategoria;
    }
}