package com.gustavointech.compras.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.nota.NotaRepository;
import com.gustavointech.compras.usuario.UsuarioService;

@Service
public class RelatorioService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final NotaRepository notas;
    private final UsuarioService usuarios;

    public RelatorioService(NotaRepository notas, UsuarioService usuarios) {
        this.notas = notas;
        this.usuarios = usuarios;
    }

    // O dashboard de um mês. Toda conta é feita aqui, no backend: o frontend só
    // mostra.
    @Transactional(readOnly = true)
    public RelatorioMesResponse mes(Long usuarioId, YearMonth mes) {
        Totais atual = totais(usuarioId, mes);
        Totais anterior = totais(usuarioId, mes.minusMonths(1));

        // Por categoria: o valor BRUTO dos itens (RN11)
        List<Object[]> linhas = notas.gastoPorCategoria(usuarioId, primeiroDia(mes), primeiroDia(mes.plusMonths(1)));
        BigDecimal valorDosItens = BigDecimal.ZERO.setScale(2);
        for (Object[] linha : linhas) {
            valorDosItens = valorDosItens.add(dinheiro(linha[2]));
        }
        List<GastoCategoriaResponse> porCategoria = new ArrayList<>();
        for (Object[] linha : linhas) {
            BigDecimal gasto = dinheiro(linha[2]);
            porCategoria.add(new GastoCategoriaResponse(
                    linha[0] == null ? null : ((Number) linha[0]).longValue(),
                    linha[1] == null ? "Sem categoria" : (String) linha[1],
                    gasto,
                    percentual(gasto, valorDosItens)));
        }

        return new RelatorioMesResponse(mes.getYear(), mes.getMonthValue(), atual.gasto, atual.quantidadeNotas,
                atual.descontos, valorDosItens, porCategoria,
                comparacao(mes.minusMonths(1), atual, anterior),
                progressoDaMeta(usuarioId, atual.gasto));
    }

    private Totais totais(Long usuarioId, YearMonth mes) {
        Object[] linha = notas.totaisDoPeriodo(usuarioId, primeiroDia(mes), primeiroDia(mes.plusMonths(1))).get(0);
        return new Totais(dinheiro(linha[0]), dinheiro(linha[1]), ((Number) linha[2]).longValue());
    }

    private static MesAnteriorResponse comparacao(YearMonth mesAnterior, Totais atual, Totais anterior) {
        BigDecimal variacao = atual.gasto.subtract(anterior.gasto);
        // Se o mês anterior não teve gasto, não dá para dizer "subiu X%": fica vazio
        BigDecimal variacaoPercentual = anterior.gasto.signum() == 0 ? null : percentual(variacao, anterior.gasto);
        return new MesAnteriorResponse(mesAnterior.getYear(), mesAnterior.getMonthValue(), anterior.gasto,
                anterior.quantidadeNotas, variacao, variacaoPercentual);
    }

    private MetaProgressoResponse progressoDaMeta(Long usuarioId, BigDecimal gasto) {
        BigDecimal meta = usuarios.meta(usuarioId);
        if (meta == null) {
            return null;
        }
        meta = meta.setScale(2, RoundingMode.HALF_UP);
        return new MetaProgressoResponse(meta, percentual(gasto, meta), meta.subtract(gasto),
                gasto.compareTo(meta) > 0);
    }

    // parte / total em %, com 1 casa decimal
    private static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }

    // Dinheiro sempre com 2 casas (o banco devolve "0" quando não há linhas)
    private static BigDecimal dinheiro(Object valor) {
        BigDecimal v = valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDate primeiroDia(YearMonth mes) {
        return mes.atDay(1);
    }

    private static final class Totais {

        private final BigDecimal gasto;
        private final BigDecimal descontos;
        private final long quantidadeNotas;

        private Totais(BigDecimal gasto, BigDecimal descontos, long quantidadeNotas) {
            this.gasto = gasto;
            this.descontos = descontos;
            this.quantidadeNotas = quantidadeNotas;
        }
    }
}