package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Uma nota completa, com mercado e itens
public class NotaDetalheResponse {

    private final Long id;
    private final String mercado;
    private final String cnpj;
    private final String endereco;
    private final LocalDate dataCompra;
    private final BigDecimal valorTotal;
    private final BigDecimal desconto;
    private final BigDecimal valorAPagar;
    private final List<ItemNotaResponse> itens;

    public NotaDetalheResponse(Long id, String mercado, String cnpj, String endereco, LocalDate dataCompra,
            BigDecimal valorTotal, BigDecimal desconto, BigDecimal valorAPagar,
            List<ItemNotaResponse> itens) {
        this.id = id;
        this.mercado = mercado;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.dataCompra = dataCompra;
        this.valorTotal = valorTotal;
        this.desconto = desconto;
        this.valorAPagar = valorAPagar;
        this.itens = itens;
    }

    public Long getId() {
        return id;
    }

    public String getMercado() {
        return mercado;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEndereco() {
        return endereco;
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

    public List<ItemNotaResponse> getItens() {
        return itens;
    }
}