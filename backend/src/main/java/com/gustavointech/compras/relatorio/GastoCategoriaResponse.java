package com.gustavointech.compras.relatorio;

import java.math.BigDecimal;

// O gasto de uma categoria no mês, pelo valor dos itens (antes do desconto). O que ainda não tem categoria
// aparece como "Sem categoria", com categoriaId vazio.
public class GastoCategoriaResponse {

    private final Long categoriaId;
    private final String categoria;
    private final BigDecimal gasto;
    private final BigDecimal percentual;

    public GastoCategoriaResponse(Long categoriaId, String categoria, BigDecimal gasto, BigDecimal percentual) {
        this.categoriaId = categoriaId;
        this.categoria = categoria;
        this.gasto = gasto;
        this.percentual = percentual;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getGasto() {
        return gasto;
    }

    // Quanto este gasto representa do valor dos itens do mês, em %, com 1 casa
    // decimal
    public BigDecimal getPercentual() {
        return percentual;
    }
}