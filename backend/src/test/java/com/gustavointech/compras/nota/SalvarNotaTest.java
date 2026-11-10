package com.gustavointech.compras.nota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.gustavointech.compras.usuario.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Salvar de verdade: usuário logado, texto de nota real (anonimizada), gravando no banco de desenvolvimento.
// @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class SalvarNotaTest {

    private static final String CHAVE_ACOUGUE = "33261008710596000125651010000000011000000010";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private NotaRepository notas;
    @PersistenceContext
    private EntityManager em;

    private MockMvc mockMvc;
    private String emailLogado;

    @BeforeEach
    void montar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private String lerArquivo(String nome) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/notas/" + nome)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    // Transforma um texto em um valor JSON (com aspas, \n, \t etc.)
    private static String aspas(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    // Cadastra um usuário novo, faz login e devolve a sessão
    private MockHttpSession entrar() throws Exception {
        emailLogado = "teste-" + UUID.randomUUID() + "@teste.com";
        String credenciais = "{\"email\":\"" + emailLogado + "\",\"senha\":\"senha-forte-123\"}";

        mockMvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    private ResultActions salvar(MockHttpSession sessao, String texto, String link, String data) throws Exception {
        StringBuilder json = new StringBuilder("{\"texto\":" + aspas(texto));
        if (link != null) {
            json.append(",\"link\":").append(aspas(link));
        }
        if (data != null) {
            json.append(",\"data\":").append(aspas(data));
        }
        json.append("}");
        return mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json.toString()));
    }

    private Long idDoUsuario() {
        return usuarios.buscarPorEmail(emailLogado).orElseThrow().getId();
    }

    private List<Nota> notasDoUsuario() {
        return notas.listarDoPeriodo(idDoUsuario(), LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1));
    }

    // Esquece a memória do Hibernate e busca a nota (com mercado e itens) de volta
    // do banco
    private Nota notaGravada() {
        em.flush();
        em.clear();
        Long idDaNota = notasDoUsuario().get(0).getId();
        em.clear();
        return notas.buscarComItens(idDaNota, idDoUsuario()).orElseThrow();
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(post("/api/notas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"qualquer coisa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void salvaANotaDoAcougueComItensEMercado() throws Exception {
        MockHttpSession sessao = entrar();

        salvar(sessao, lerArquivo("acougue-computador.txt"), null, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.mercado").value("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME"))
                .andExpect(jsonPath("$.dataCompra").value("2026-10-25"))
                .andExpect(jsonPath("$.valorAPagar").value(196.8))
                .andExpect(jsonPath("$.quantidadeItens").value(10));

        Nota gravada = notaGravada();
        assertEquals(CHAVE_ACOUGUE, gravada.getIdentificador());
        assertEquals(CHAVE_ACOUGUE, gravada.getChaveAcesso());
        assertEquals(10, gravada.getItens().size());
        assertEquals("08710596000125", gravada.getMercado().getCnpj());
        assertEquals(0, new BigDecimal("196.80").compareTo(gravada.getValorTotal()));
    }

    @Test
    void amesmaNotaDuasVezesDevolve409() throws Exception {
        MockHttpSession sessao = entrar();
        String texto = lerArquivo("acougue-computador.txt");
        salvar(sessao, texto, null, null).andExpect(status().isCreated());

        salvar(sessao, texto, null, null).andExpect(status().isConflict());

        assertEquals(1, notasDoUsuario().size());
    }

    @Test
    void notaDoCelularSemDataDevolve422ENaoGrava() throws Exception {
        MockHttpSession sessao = entrar();

        salvar(sessao, lerArquivo("acougue-celular.txt"), null, null)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title").value("Nota inválida"));

        assertTrue(notasDoUsuario().isEmpty(), "nada deveria ter sido gravado");
    }

    @Test
    void notaDoCelularComDataInformadaGravaComImpressaoDigital() throws Exception {
        MockHttpSession sessao = entrar();

        salvar(sessao, lerArquivo("acougue-celular.txt"), null, "2026-10-25")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataCompra").value("2026-10-25"));

        Nota gravada = notaGravada();
        assertEquals(64, gravada.getIdentificador().length());
        assertNull(gravada.getChaveAcesso());
    }

    @Test
    void amesmaNotaEmOutraVersaoDaPaginaDevolve409() throws Exception {
        MockHttpSession sessao = entrar();
        salvar(sessao, lerArquivo("acougue-celular.txt"), null, "2026-10-25").andExpect(status().isCreated());

        // A versão do computador tem chave, mas é a mesma compra: o conteúdo denuncia
        salvar(sessao, lerArquivo("acougue-computador.txt"), null, null).andExpect(status().isConflict());

        assertEquals(1, notasDoUsuario().size());
    }

    @Test
    void notaQueNaoConfereDevolve422ENaoGrava() throws Exception {
        MockHttpSession sessao = entrar();
        String semCebola = lerArquivo("acougue-computador.txt").replaceFirst("Cebola kg[\\s\\S]*?4,35\\n", "");

        salvar(sessao, semCebola, null, null)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title").value("Nota inválida"));

        assertTrue(notasDoUsuario().isEmpty(), "nada deveria ter sido gravado");
    }

    @Test
    void dataNoFuturoDevolve422() throws Exception {
        MockHttpSession sessao = entrar();
        String amanha = LocalDate.now().plusDays(1).toString();

        salvar(sessao, lerArquivo("acougue-celular.txt"), null, amanha).andExpect(status().is(422));

        assertTrue(notasDoUsuario().isEmpty());
    }

    @Test
    void dataQueNaoBateComOMesDaChaveDevolve422() throws Exception {
        MockHttpSession sessao = entrar();
        String link = "https://exemplo.gov.br/consulta?p=" + CHAVE_ACOUGUE + "|2|1|1|" + "0".repeat(40);

        // A chave diz outubro de 2026; a data informada é de setembro
        salvar(sessao, lerArquivo("acougue-celular.txt"), link, "2026-09-15").andExpect(status().is(422));

        assertTrue(notasDoUsuario().isEmpty());
    }

    @Test
    void doisUsuariosPodemSalvarAMesmaNotaEOMercadoEReaproveitado() throws Exception {
        String texto = lerArquivo("acougue-computador.txt");

        MockHttpSession primeiro = entrar();
        salvar(primeiro, texto, null, null).andExpect(status().isCreated());

        // Se o mercado fosse criado de novo, o CNPJ repetido estouraria no banco
        MockHttpSession segundo = entrar();
        salvar(segundo, texto, null, null).andExpect(status().isCreated());
    }

    @Test
    void salvaANotaDoAtacadistaComDesconto() throws Exception {
        MockHttpSession sessao = entrar();

        salvar(sessao, lerArquivo("atacadista.txt"), null, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidadeItens").value(63));

        Nota gravada = notaGravada();
        assertEquals(63, gravada.getItens().size());
        assertEquals(0, new BigDecimal("589.91").compareTo(gravada.getValorTotal()));
        assertEquals(0, new BigDecimal("16.51").compareTo(gravada.getDesconto()));
        assertEquals(0, new BigDecimal("573.40").compareTo(gravada.getValorAPagar()));
    }
}