package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gustavointech.compras.leitor.ItemLido;
import com.gustavointech.compras.leitor.NotaLida;

// Decide como uma nota é identificada para impedir o registro duplicado (RN06).
final class IdentificadorNota {

    private static final Pattern CHAVE_NO_LINK = Pattern.compile("[?&]p=(\\d{44})");

    private IdentificadorNota() {
    }

    // O link do QR Code carrega a chave de 44 dígitos: ...?p=<chave>|2|1|1|<hash>
    static String chaveDoLink(String link) {
        if (link == null) {
            return null;
        }
        Matcher m = CHAVE_NO_LINK.matcher(link);
        return m.find() ? m.group(1) : null;
    }

    // A chave do texto vale mais que a do link; se nenhuma existir, devolve null
    static String chave(NotaLida nota, String link) {
        return nota.getChaveAcesso() != null ? nota.getChaveAcesso() : chaveDoLink(link);
    }

    // 1) a chave de 44 dígitos; 2) CNPJ|série|número; 3) null (aí vale a impressão digital)
    static String de(NotaLida nota, String chave) {
        if (chave != null) {
            return chave;
        }
        String cnpj = nota.getMercado().getCnpj();
        if (cnpj != null && nota.getSerie() != null && nota.getNumero() != null) {
            return cnpj + "|" + nota.getSerie() + "|" + nota.getNumero();
        }
        return null;
    }

    // "Impressão digital" do conteúdo: o mesmo mercado, data, total e itens dão sempre o mesmo código
    // (64 caracteres). Serve quando o texto não traz chave nem número, e para reconhecer a mesma nota
    // copiada de outra versão da página.
    static String impressaoDigital(NotaLida nota, LocalDate data) {
        StringBuilder base = new StringBuilder();
        base.append(nota.getMercado().getCnpj()).append('|').append(data)
                .append('|').append(numero(nota.getValorAPagar()));
        for (ItemLido item : nota.getItens()) {
            base.append('|').append(item.getCodigo())
                    .append(':').append(item.getNomeNormalizado())
                    .append(':').append(numero(item.getQuantidade()))
                    .append(':').append(numero(item.getTotal()));
        }
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(base.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // 196.80 e 196.8 são o mesmo número: tira os zeros à direita antes de montar o texto
    private static String numero(BigDecimal valor) {
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }
}