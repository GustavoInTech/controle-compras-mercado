package com.gustavointech.compras.nota;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // 1) a chave de 44 dígitos; 2) CNPJ|série|número; 3) null (o salvamento usará
    // uma impressão digital com a data)
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
}