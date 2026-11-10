package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.LocalDate;

// O que o sistema devolve depois de salvar: um resumo da nota registrada
public class NotaSalvaResponse {

    private final Long id;
    private final String mercado;
    private final LocalDate dataCompra;
    private final BigDecimal valorTotal;
    private final BigDecimal desconto;
    private final BigDecimal valorAPagar;
    private final int quantidadeItens;

    public NotaSalvaResponse(Long id, String mercado, LocalDate dataCompra, BigDecimal valorTotal,
            BigDecimal desconto, BigDecimal valorAPagar, int quantidadeItens) {
        this.id = id;
        this.mercado = mercado;
        this.dataCompra = dataCompra;
        this.valorTotal = valorTotal;
        this.desconto = desconto;
        this.valorAPagar = valorAPagar;
        this.quantidadeItens = quantidadeItens;
    }

    public Long getId() {
        return id;
    }

    public String getMercado() {
        return mercado;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getValorAPagar() {
        return valorAPagar;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }
}