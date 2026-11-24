package com.gustavointech.compras.relatorio;

import java.math.BigDecimal;

// Quanto da meta mensal já foi usado
public class MetaProgressoResponse {

    private final BigDecimal valor;
    private final BigDecimal percentualUsado;
    private final BigDecimal restante;
    private final boolean ultrapassada;

    public MetaProgressoResponse(BigDecimal valor, BigDecimal percentualUsado, BigDecimal restante,
            boolean ultrapassada) {
        this.valor = valor;
        this.percentualUsado = percentualUsado;
        this.restante = restante;
        this.ultrapassada = ultrapassada;
    }

    public BigDecimal getValor() {
        return valor;
    }

    // Pode passar de 100
    public BigDecimal getPercentualUsado() {
        return percentualUsado;
    }

    // Meta menos o gasto; negativo quando a meta foi ultrapassada
    public BigDecimal getRestante() {
        return restante;
    }

    public boolean isUltrapassada() {
        return ultrapassada;
    }
}