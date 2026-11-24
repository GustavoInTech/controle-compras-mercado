package com.gustavointech.compras.relatorio;

import java.math.BigDecimal;
import java.util.List;

// O dashboard de um mês (RN11): o gasto do mês é o que foi PAGO; o gasto por categoria soma os itens pelo valor
// BRUTO (a nota não diz a qual item o desconto se aplica). A conta fecha assim:
// valorDosItens - descontos = gastoTotal.
public class RelatorioMesResponse {

    private final int ano;
    private final int mes;
    private final BigDecimal gastoTotal;
    private final long quantidadeNotas;
    private final BigDecimal descontos;
    private final BigDecimal valorDosItens;
    private final List<GastoCategoriaResponse> porCategoria;
    private final MesAnteriorResponse mesAnterior;
    private final MetaProgressoResponse meta;

    public RelatorioMesResponse(int ano, int mes, BigDecimal gastoTotal, long quantidadeNotas, BigDecimal descontos,
            BigDecimal valorDosItens, List<GastoCategoriaResponse> porCategoria,
            MesAnteriorResponse mesAnterior, MetaProgressoResponse meta) {
        this.ano = ano;
        this.mes = mes;
        this.gastoTotal = gastoTotal;
        this.quantidadeNotas = quantidadeNotas;
        this.descontos = descontos;
        this.valorDosItens = valorDosItens;
        this.porCategoria = porCategoria;
        this.mesAnterior = mesAnterior;
        this.meta = meta;
    }

    public int getAno() {
        return ano;
    }

    public int getMes() {
        return mes;
    }

    // A soma do valor pago das notas do mês
    public BigDecimal getGastoTotal() {
        return gastoTotal;
    }

    public long getQuantidadeNotas() {
        return quantidadeNotas;
    }

    public BigDecimal getDescontos() {
        return descontos;
    }

    // A soma de todos os itens, antes do desconto (é a soma das categorias)
    public BigDecimal getValorDosItens() {
        return valorDosItens;
    }

    public List<GastoCategoriaResponse> getPorCategoria() {
        return porCategoria;
    }

    public MesAnteriorResponse getMesAnterior() {
        return mesAnterior;
    }

    // null quando o usuário não definiu uma meta mensal
    public MetaProgressoResponse getMeta() {
        return meta;
    }
}