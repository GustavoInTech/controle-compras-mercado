package com.gustavointech.compras.leitor;

import java.math.BigDecimal;
import java.util.Objects;

public final class ItemLido {

    private final String nome;
    private final String nomeNormalizado;
    private final String codigo;
    private final BigDecimal quantidade;
    private final String unidade;
    private final BigDecimal precoUnitario;
    private final BigDecimal total;

    public ItemLido(String nome,
            String nomeNormalizado,
            String codigo,
            BigDecimal quantidade,
            String unidade,
            BigDecimal precoUnitario,
            BigDecimal total) {
        this.nome = nome;
        this.nomeNormalizado = nomeNormalizado;
        this.codigo = codigo;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.total = total;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemLido that))
            return false;
        return Objects.equals(nome, that.nome)
                && Objects.equals(nomeNormalizado, that.nomeNormalizado)
                && Objects.equals(codigo, that.codigo)
                && Objects.equals(quantidade, that.quantidade)
                && Objects.equals(unidade, that.unidade)
                && Objects.equals(precoUnitario, that.precoUnitario)
                && Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, nomeNormalizado, codigo, quantidade, unidade, precoUnitario, total);
    }

    @Override
    public String toString() {
        return "ItemLido{nome=" + nome
                + ", nomeNormalizado=" + nomeNormalizado
                + ", codigo=" + codigo
                + ", quantidade=" + quantidade
                + ", unidade=" + unidade
                + ", precoUnitario=" + precoUnitario
                + ", total=" + total + "}";
    }
}