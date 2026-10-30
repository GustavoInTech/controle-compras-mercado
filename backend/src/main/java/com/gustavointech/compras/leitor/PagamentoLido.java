package com.gustavointech.compras.leitor;

import java.math.BigDecimal;
import java.util.Objects;

public final class PagamentoLido {

    private final String forma;
    private final BigDecimal valor;

    public PagamentoLido(String forma, BigDecimal valor) {
        this.forma = forma;
        this.valor = valor;
    }

    public String getForma() {
        return forma;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PagamentoLido that))
            return false;
        return Objects.equals(forma, that.forma)
                && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forma, valor);
    }

    @Override
    public String toString() {
        return "PagamentoLido{forma=" + forma + ", valor=" + valor + "}";
    }
}