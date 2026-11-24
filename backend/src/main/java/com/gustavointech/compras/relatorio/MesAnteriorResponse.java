package com.gustavointech.compras.relatorio;

import java.math.BigDecimal;

// O mês anterior e a comparação com o mês do relatório
public class MesAnteriorResponse {

    private final int ano;
    private final int mes;
    private final BigDecimal gastoTotal;
    private final long quantidadeNotas;
    private final BigDecimal variacao;
    private final BigDecimal variacaoPercentual;

    public MesAnteriorResponse(int ano, int mes, BigDecimal gastoTotal, long quantidadeNotas,
            BigDecimal variacao, BigDecimal variacaoPercentual) {
        this.ano = ano;
        this.mes = mes;
        this.gastoTotal = gastoTotal;
        this.quantidadeNotas = quantidadeNotas;
        this.variacao = variacao;
        this.variacaoPercentual = variacaoPercentual;
    }

    public int getAno() {
        return ano;
    }

    public int getMes() {
        return mes;
    }

    public BigDecimal getGastoTotal() {
        return gastoTotal;
    }

    public long getQuantidadeNotas() {
        return quantidadeNotas;
    }

    // Gasto do mês do relatório menos o do mês anterior (positivo = gastou mais)
    public BigDecimal getVariacao() {
        return variacao;
    }

    // Em %, com 1 casa decimal; vazio quando o mês anterior não teve gasto (não dá
    // para dividir por zero)
    public BigDecimal getVariacaoPercentual() {
        return variacaoPercentual;
    }
}