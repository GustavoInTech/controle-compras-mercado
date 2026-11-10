package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.gustavointech.compras.leitor.ItemLido;
import com.gustavointech.compras.leitor.MercadoLido;
import com.gustavointech.compras.leitor.PagamentoLido;

// O que o sistema devolve na prévia: o que foi lido, se confere e se a nota já foi registrada.
// Não leva o texto original nem o CPF.
public class PreviaNotaResponse {

    private final MercadoLido mercado;
    private final String identificador;
    private final LocalDate dataEmissao;
    private final List<ItemLido> itens;
    private final BigDecimal valorTotal;
    private final BigDecimal desconto;
    private final BigDecimal valorAPagar;
    private final List<PagamentoLido> pagamentos;
    private final List<String> avisos;
    private final boolean conferida;
    private final boolean precisaDeData;
    private final Boolean jaRegistrada;

    public PreviaNotaResponse(MercadoLido mercado,
            String identificador,
            LocalDate dataEmissao,
            List<ItemLido> itens,
            BigDecimal valorTotal,
            BigDecimal desconto,
            BigDecimal valorAPagar,
            List<PagamentoLido> pagamentos,
            List<String> avisos,
            boolean conferida,
            boolean precisaDeData,
            Boolean jaRegistrada) {
        this.mercado = mercado;
        this.identificador = identificador;
        this.dataEmissao = dataEmissao;
        this.itens = itens;
        this.valorTotal = valorTotal;
        this.desconto = desconto;
        this.valorAPagar = valorAPagar;
        this.pagamentos = pagamentos;
        this.avisos = avisos;
        this.conferida = conferida;
        this.precisaDeData = precisaDeData;
        this.jaRegistrada = jaRegistrada;
    }

    public MercadoLido getMercado() {
        return mercado;
    }

    // null quando o texto não traz como identificar a nota (o salvamento resolve
    // com a data informada)
    public String getIdentificador() {
        return identificador;
    }

    // null na versão do celular sem "Informações gerais da Nota"
    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public List<ItemLido> getItens() {
        return itens;
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

    // true: tem itens e nenhuma conferência falhou
    public boolean isConferida() {
        return conferida;
    }

    // true: a data não veio no texto e o usuário precisa informar o dia
    public boolean isPrecisaDeData() {
        return precisaDeData;
    }

    // true/false; null quando ainda não dá para saber (sem identificador)
    public Boolean getJaRegistrada() {
        return jaRegistrada;
    }
}