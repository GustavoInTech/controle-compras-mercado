package com.gustavointech.compras.nota;

import java.math.BigDecimal;

// Um item de uma nota, como aparece no detalhe
public class ItemNotaResponse {

    private final Long id;
    private final String nome;
    private final String codigo;
    private final BigDecimal quantidade;
    private final String unidade;
    private final BigDecimal precoUnitario;
    private final BigDecimal total;

    public ItemNotaResponse(Long id, String nome, String codigo, BigDecimal quantidade,
            String unidade, BigDecimal precoUnitario, BigDecimal total) {
        this.id = id;
        this.nome = nome;
        this.codigo = codigo;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }
}