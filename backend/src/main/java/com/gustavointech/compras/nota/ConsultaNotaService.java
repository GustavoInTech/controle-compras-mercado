package com.gustavointech.compras.nota;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.mercado.Mercado;

@Service
public class ConsultaNotaService {

    private final NotaRepository notas;

    public ConsultaNotaService(NotaRepository notas) {
        this.notas = notas;
    }

    @Transactional(readOnly = true)
    public List<NotaResumoResponse> listar(Long usuarioId, YearMonth mes) {
        List<Nota> doMes = notas.listarDoPeriodo(usuarioId, mes.atDay(1), mes.plusMonths(1).atDay(1));
        if (doMes.isEmpty()) {
            return List.of();
        }

        // Uma consulta só para contar os itens de todas as notas (evita uma consulta
        // por nota)
        Map<Long, Long> quantidades = new HashMap<>();
        for (Object[] linha : notas.contarItens(doMes.stream().map(Nota::getId).toList())) {
            quantidades.put((Long) linha[0], (Long) linha[1]);
        }

        return doMes.stream()
                .map(n -> new NotaResumoResponse(n.getId(), n.getMercado().getNome(), n.getDataCompra(),
                        n.getValorTotal(), n.getDesconto(), n.getValorAPagar(),
                        quantidades.getOrDefault(n.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public NotaDetalheResponse detalhar(Long usuarioId, Long id) {
        Nota nota = notas.buscarComItens(id, usuarioId).orElseThrow(NotaNaoEncontradaException::new);
        Mercado mercado = nota.getMercado();

        List<ItemNotaResponse> itens = nota.getItens().stream()
                .map(i -> new ItemNotaResponse(i.getId(), i.getNomeOriginal(), i.getCodigoMercado(),
                        i.getQuantidade(), i.getUnidade(), i.getPrecoUnitario(), i.getTotal()))
                .toList();

        return new NotaDetalheResponse(nota.getId(), mercado.getNome(), mercado.getCnpj(), mercado.getEndereco(),
                nota.getDataCompra(), nota.getValorTotal(), nota.getDesconto(), nota.getValorAPagar(), itens);
    }

    // Apaga a nota e, junto, os itens dela. Se a nota não for deste usuário, é como
    // se não existisse.
    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Nota nota = notas.buscarComItens(id, usuarioId).orElseThrow(NotaNaoEncontradaException::new);
        notas.delete(nota);
    }
}