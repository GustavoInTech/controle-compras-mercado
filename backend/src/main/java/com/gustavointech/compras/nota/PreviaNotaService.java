package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.leitor.LeitorNfce;
import com.gustavointech.compras.leitor.NotaLida;

@Service
public class PreviaNotaService {

    // O leitor é Java puro e não guarda estado: pode ser criado aqui mesmo
    private final LeitorNfce leitor = new LeitorNfce();
    private final NotaRepository notas;

    public PreviaNotaService(NotaRepository notas) {
        this.notas = notas;
    }

    // Lê o texto, confere e diz se a nota já foi registrada. NÃO grava nada.
    @Transactional(readOnly = true)
    public PreviaNotaResponse gerar(Long usuarioId, String texto, String link) {
        NotaLida nota = leitor.ler(texto);
        List<String> avisos = new ArrayList<>(nota.getAvisos());

        String chave = IdentificadorNota.chave(nota, link);
        String cnpj = nota.getMercado().getCnpj();
        // Posições 6 a 19 da chave de 44 dígitos são o CNPJ do mercado
        if (chave != null && cnpj != null && !chave.substring(6, 20).equals(cnpj)) {
            avisos.add("A chave da nota não bate com o CNPJ do texto: o texto pode ser de outra nota.");
        }

        String identificador = IdentificadorNota.de(nota, chave);
        Boolean jaRegistrada = identificador == null
                ? null
                : notas.existsByUsuarioIdAndIdentificador(usuarioId, identificador);

        // Sem a linha "Valor total" (nota sem desconto), o total é o valor a pagar
        BigDecimal valorTotal = nota.getValorTotal() != null ? nota.getValorTotal() : nota.getValorAPagar();

        boolean conferida = !nota.getItens().isEmpty() && avisos.isEmpty();
        boolean precisaDeData = nota.getDataEmissao() == null;

        return new PreviaNotaResponse(nota.getMercado(), identificador, nota.getDataEmissao(), nota.getItens(),
                valorTotal, nota.getDesconto(), nota.getValorAPagar(), nota.getPagamentos(), avisos,
                conferida, precisaDeData, jaRegistrada);
    }
}