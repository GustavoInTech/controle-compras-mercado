package com.gustavointech.compras.leitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LeitorNfceTest {

    private final LeitorNfce leitor = new LeitorNfce();

    // Lê um arquivo da pasta src/test/resources/notas.
    // Troca a quebra de linha do Windows (\r\n) pela comum (\n) para o teste ser
    // igual em qualquer computador.
    private String lerArquivo(String nome) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/notas/" + nome)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    // Compara dinheiro pelo valor (196.8 e 196.80 são o mesmo valor)
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

    @Test
    void leNotaDoAcougueVersaoCelularSemDataENemChave() throws IOException {
        NotaLida nota = leitor.ler(lerArquivo("acougue-celular.txt"));

        assertEquals(10, nota.getItens().size());
        assertValor("196.80", nota.getValorAPagar());
        assertNull(nota.getDataEmissao());
        assertNull(nota.getChaveAcesso());
        assertTrue(nota.getAvisos().isEmpty(), "não deveria ter avisos: " + nota.getAvisos());
    }

    @Test
    void leOsCamposDeUmItem() throws IOException {
        NotaLida nota = leitor.ler(lerArquivo("acougue-computador.txt"));
        ItemLido beterraba = nota.getItens().get(0);

        assertEquals("Beterraba kg", beterraba.getNome());
        assertEquals("BETERRABA KG", beterraba.getNomeNormalizado());
        assertEquals("8885", beterraba.getCodigo());
        assertEquals("KG", beterraba.getUnidade());
        assertValor("0.245", beterraba.getQuantidade());
        assertValor("3.99", beterraba.getPrecoUnitario());
        // A nota cobra 0,97, mesmo que 0,245 x 3,99 dê 0,97755: vale o total impresso
        assertValor("0.97", beterraba.getTotal());
    }

    @Test
    void leNotaDoAtacadistaComDesconto() throws IOException {
        NotaLida nota = leitor.ler(lerArquivo("atacadista.txt"));

        assertEquals("SENDAS DISTRIBUIDORA S/A", nota.getMercado().getNome());
        assertEquals(LocalDate.of(2026, 10, 3), nota.getDataEmissao());
        assertEquals(63, nota.getItens().size());
        assertValor("589.91", nota.getValorTotal());
        assertValor("16.51", nota.getDesconto());
        assertValor("573.40", nota.getValorAPagar());
        assertEquals(1, nota.getPagamentos().size());
        assertEquals("Vale Alimentação", nota.getPagamentos().get(0).getForma());
        assertValor("573.40", nota.getPagamentos().get(0).getValor());
        assertTrue(nota.getAvisos().isEmpty(), "não deveria ter avisos: " + nota.getAvisos());
    }

    @Test
    void geraAvisoQuandoFaltaUmItem() throws IOException {
        // Tira o item "Cebola" do texto, como se a pessoa tivesse copiado a nota pela
        // metade
        String semCebola = lerArquivo("acougue-computador.txt").replaceFirst("Cebola kg[\\s\\S]*?4,35\\n", "");

        NotaLida nota = leitor.ler(semCebola);

        assertEquals(9, nota.getItens().size());
        assertFalse(nota.getAvisos().isEmpty(), "deveria avisar que a nota não confere");
        assertTrue(nota.getAvisos().stream().anyMatch(a -> a.contains("A nota diz 10 itens")),
                "avisos: " + nota.getAvisos());
    }

    @Test
    void naoDevolveCpfNoResultado() throws IOException {
        String texto = lerArquivo("atacadista.txt");
        // Garante que o arquivo de teste realmente tem um CPF; senão o teste não prova
        // nada
        assertTrue(texto.contains("000.000.000-00"), "o arquivo de teste deveria conter um CPF falso");

        NotaLida nota = leitor.ler(texto);
        String tudo = nota + " " + nota.getMercado() + " " + nota.getItens() + " "
                + nota.getPagamentos() + " " + nota.getAvisos();

        assertFalse(tudo.contains("000.000.000-00"), "o CPF não pode aparecer no resultado");
    }

    @Test
    void textoQualquerGeraAvisosEnaoQuebra() {
        NotaLida nota = leitor.ler("isto não é uma nota fiscal");

        assertTrue(nota.getItens().isEmpty());
        assertFalse(nota.getAvisos().isEmpty());
    }

    @Test
    void funcionaComQuebraDeLinhaDoWindows() throws IOException {
        String comCrLf = lerArquivo("acougue-computador.txt").replace("\n", "\r\n");

        NotaLida nota = leitor.ler(comCrLf);

        assertEquals(10, nota.getItens().size());
        assertTrue(nota.getAvisos().isEmpty(), "não deveria ter avisos: " + nota.getAvisos());
    }
}