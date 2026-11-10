package com.gustavointech.compras.nota;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.usuario.Usuario;
import com.gustavointech.compras.usuario.UsuarioRepository;

// A prévia de verdade: usuário logado, texto de uma nota real (anonimizada), passando pela segurança.
@SpringBootTest
@Transactional
class PreviaNotaTest {

    private static final String CHAVE_ACOUGUE = "33261008710596000125651010000000011000000010";
    // Chave de outro mercado (o atacadista): o CNPJ dela não bate com o do açougue
    private static final String CHAVE_OUTRO_CNPJ = "33261006057223033266650190000000011000000010";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private MercadoRepository mercados;
    @Autowired
    private NotaRepository notas;

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

    private ResultActions previa(MockHttpSession sessao, String texto, String link) throws Exception {
        String json = "{\"texto\":" + aspas(texto) + (link == null ? "" : ",\"link\":" + aspas(link)) + "}";
        return mockMvc.perform(post("/api/notas/previa").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(post("/api/notas/previa").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"qualquer coisa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devolvePreviaConferidaDaNotaDoAcougue() throws Exception {
        MockHttpSession sessao = entrar();

        previa(sessao, lerArquivo("acougue-computador.txt"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conferida").value(true))
                .andExpect(jsonPath("$.mercado.nome").value("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME"))
                .andExpect(jsonPath("$.identificador").value(CHAVE_ACOUGUE))
                .andExpect(jsonPath("$.dataEmissao").value("2026-10-25"))
                .andExpect(jsonPath("$.itens.length()").value(10))
                .andExpect(jsonPath("$.valorAPagar").value(196.8))
                .andExpect(jsonPath("$.valorTotal").value(196.8))
                .andExpect(jsonPath("$.precisaDeData").value(false))
                .andExpect(jsonPath("$.jaRegistrada").value(false))
                .andExpect(jsonPath("$.avisos.length()").value(0));
    }

    @Test
    void notaDoCelularSemDataNemChaveFicaSemIdentificador() throws Exception {
        MockHttpSession sessao = entrar();

        previa(sessao, lerArquivo("acougue-celular.txt"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conferida").value(true))
                .andExpect(jsonPath("$.precisaDeData").value(true))
                .andExpect(jsonPath("$.dataEmissao").doesNotExist())
                .andExpect(jsonPath("$.identificador").doesNotExist())
                .andExpect(jsonPath("$.jaRegistrada").doesNotExist());
    }

    @Test
    void usaAChaveDoLinkQuandoOTextoNaoTem() throws Exception {
        MockHttpSession sessao = entrar();
        String link = "https://exemplo.gov.br/consulta?p=" + CHAVE_ACOUGUE + "|2|1|1|" + "0".repeat(40);

        previa(sessao, lerArquivo("acougue-celular.txt"), link)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificador").value(CHAVE_ACOUGUE))
                .andExpect(jsonPath("$.jaRegistrada").value(false))
                .andExpect(jsonPath("$.conferida").value(true));
    }

    @Test
    void avisaQuandoAChaveDoLinkNaoBateComOMercado() throws Exception {
        MockHttpSession sessao = entrar();
        String link = "https://exemplo.gov.br/consulta?p=" + CHAVE_OUTRO_CNPJ + "|2|1|1|" + "0".repeat(40);

        previa(sessao, lerArquivo("acougue-celular.txt"), link)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conferida").value(false))
                .andExpect(jsonPath("$.avisos.length()").value(1));
    }

    @Test
    void dizQueANotaJaFoiRegistradaPeloUsuario() throws Exception {
        MockHttpSession sessao = entrar();
        Usuario usuario = usuarios.buscarPorEmail(emailLogado).orElseThrow();
        Mercado mercado = mercados.saveAndFlush(new Mercado("99999999999999", "Mercado de Teste", "Rua do Teste, 1"));
        notas.saveAndFlush(new Nota(usuario, mercado, CHAVE_ACOUGUE, CHAVE_ACOUGUE, LocalDate.of(2026, 10, 25),
                new BigDecimal("196.80"), BigDecimal.ZERO, new BigDecimal("196.80")));

        previa(sessao, lerArquivo("acougue-computador.txt"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jaRegistrada").value(true));
    }

    @Test
    void textoVazioDevolve400() throws Exception {
        MockHttpSession sessao = entrar();

        previa(sessao, "", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Pedido inválido"));
    }

    @Test
    void textoQueNaoENotaDevolveAvisosEnaoQuebra() throws Exception {
        MockHttpSession sessao = entrar();

        previa(sessao, "isto não é uma nota fiscal", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conferida").value(false))
                .andExpect(jsonPath("$.itens.length()").value(0))
                .andExpect(jsonPath("$.avisos").isNotEmpty());
    }

    @Test
    void naoDevolveOCpfNaResposta() throws Exception {
        MockHttpSession sessao = entrar();
        String texto = lerArquivo("atacadista.txt");
        // Garante que o arquivo de teste realmente tem um CPF; senão o teste não prova
        // nada
        assertTrue(texto.contains("000.000.000-00"), "o arquivo de teste deveria conter um CPF falso");

        String corpo = previa(sessao, texto, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(corpo.contains("000.000.000-00"), "o CPF não pode aparecer na resposta");
    }
}