package com.gustavointech.compras.leitor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class NotaLida {

    private final MercadoLido mercado;
    private final String chaveAcesso;
    private final LocalDate dataEmissao;
    private final List<ItemLido> itens;
    private final Integer quantidadeItensNota;
    private final BigDecimal valorTotal;
    private final BigDecimal desconto;
    private final BigDecimal valorAPagar;
    private final List<PagamentoLido> pagamentos;
    private final List<String> avisos;

    public NotaLida(MercadoLido mercado,
            String chaveAcesso,
            LocalDate dataEmissao,
            List<ItemLido> itens,
            Integer quantidadeItensNota,
            BigDecimal valorTotal,
            BigDecimal desconto,
            BigDecimal valorAPagar,
            List<PagamentoLido> pagamentos,
            List<String> avisos) {
        this.mercado = mercado;
        this.chaveAcesso = chaveAcesso;
        this.dataEmissao = dataEmissao;
        // List.copyOf cria uma cópia que não pode ser alterada; null vira lista vazia
        this.itens = itens == null ? List.of() : List.copyOf(itens);
        this.quantidadeItensNota = quantidadeItensNota;
        this.valorTotal = valorTotal;
        this.desconto = desconto;
        this.valorAPagar = valorAPagar;
        this.pagamentos = pagamentos == null ? List.of() : List.copyOf(pagamentos);
        this.avisos = avisos == null ? List.of() : List.copyOf(avisos);
    }

    public MercadoLido getMercado() {
        return mercado;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public List<ItemLido> getItens() {
        return itens;
    }

    public Integer getQuantidadeItensNota() {
        return quantidadeItensNota;
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

    public List<PagamentoLido> getPagamentos() {
        return pagamentos;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NotaLida that))
            return false;
        return Objects.equals(mercado, that.mercado)
                && Objects.equals(chaveAcesso, that.chaveAcesso)
                && Objects.equals(dataEmissao, that.dataEmissao)
                && Objects.equals(itens, that.itens)
                && Objects.equals(quantidadeItensNota, that.quantidadeItensNota)
                && Objects.equals(valorTotal, that.valorTotal)
                && Objects.equals(desconto, that.desconto)
                && Objects.equals(valorAPagar, that.valorAPagar)
                && Objects.equals(pagamentos, that.pagamentos)
                && Objects.equals(avisos, that.avisos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mercado, chaveAcesso, dataEmissao, itens, quantidadeItensNota,
                valorTotal, desconto, valorAPagar, pagamentos, avisos);
    }

    @Override
    public String toString() {
        return "NotaLida{mercado=" + mercado
                + ", chaveAcesso=" + chaveAcesso
                + ", dataEmissao=" + dataEmissao
                + ", itens=" + itens.size()
                + ", quantidadeItensNota=" + quantidadeItensNota
                + ", valorTotal=" + valorTotal
                + ", desconto=" + desconto
                + ", valorAPagar=" + valorAPagar
                + ", pagamentos=" + pagamentos
                + ", avisos=" + avisos + "}";
    }
}