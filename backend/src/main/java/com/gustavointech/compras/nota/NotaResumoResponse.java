package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.LocalDate;

// Uma nota na lista do mês: só o resumo, sem os itens
public class NotaResumoResponse {

    private final Long id;
    private final String mercado;
    private final LocalDate dataCompra;
    private final BigDecimal valorTotal;
    private final BigDecimal desconto;
    private final BigDecimal valorAPagar;
    private final long quantidadeItens;

    public NotaResumoResponse(Long id, String mercado, LocalDate dataCompra, BigDecimal valorTotal,
            BigDecimal desconto, BigDecimal valorAPagar, long quantidadeItens) {
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

    public long getQuantidadeItens() {
        return quantidadeItens;
    }
}