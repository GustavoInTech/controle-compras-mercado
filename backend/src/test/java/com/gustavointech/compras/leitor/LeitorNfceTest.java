package com.gustavointech.compras.leitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LeitorNfceTest {

    private final LeitorNfce leitor = new LeitorNfce();

    private String lerArquivo(String nome) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/notas/" + nome)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertValor(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "esperado " + esperado + " mas veio " + real);
    }

    @Test
    void leNotaDoAcougueVersaoComputador() throws IOException {
        NotaLida nota = leitor.ler(lerArquivo("acougue-computador.txt"));

        assertEquals("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME", nota.getMercado().getNome());
        assertEquals("08710596000125", nota.getMercado().getCnpj());
        assertEquals(LocalDate.of(2026, 10, 25), nota.getDataEmissao());
        assertEquals("33261008710596000125651010000000011000000010", nota.getChaveAcesso());
        assertEquals(10, nota.getItens().size());
        assertValor("196.80", nota.getValorAPagar());
        assertTrue(nota.getAvisos().isEmpty(), "não deveria ter avisos: " + nota.getAvisos());
    }
}