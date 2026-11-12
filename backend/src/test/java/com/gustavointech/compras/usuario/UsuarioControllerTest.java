package com.gustavointech.compras.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.nota.NotaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Meta mensal e exclusão de conta, passando pela segurança. @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class UsuarioControllerTest {

    private static final String SENHA = "senha-forte-123";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private NotaRepository notas;
    @Autowired
    private MercadoRepository mercados;
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

    private static String credenciais(String email) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}";
    }

    // Cadastra um usuário novo, faz login e devolve a sessão
    private MockHttpSession entrar() throws Exception {
        emailLogado = "teste-" + UUID.randomUUID() + "@teste.com";
        mockMvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais(emailLogado)))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais(emailLogado)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    // Salva a nota do açougue pela API e devolve o id dela
    private long salvarNota(MockHttpSession sessao) throws Exception {
        String corpo = mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":" + aspas(lerArquivo("acougue-computador.txt")) + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(corpo);
        assertTrue(m.find(), "a resposta deveria ter o id: " + corpo);
        return Long.parseLong(m.group(1));
    }

    private void definirMeta(MockHttpSession sessao, String json, int esperado) throws Exception {
        mockMvc.perform(put("/api/usuario/meta").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(esperado));
    }

    private void excluirConta(MockHttpSession sessao, String senha, int esperado) throws Exception {
        mockMvc.perform(post("/api/usuario/excluir").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"" + senha + "\"}"))
                .andExpect(status().is(esperado));
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/usuario/meta")).andExpect(status().isUnauthorized());
    }

    @Test
    void ametaComecaVazia() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/usuario/meta").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMensal").doesNotExist());
    }

    @Test
    void defineELeAMeta() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(put("/api/usuario/meta").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":1500.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMensal").value(1500.5));

        mockMvc.perform(get("/api/usuario/meta").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMensal").value(1500.5));
    }

    @Test
    void metaInvalidaDevolve400() throws Exception {
        MockHttpSession sessao = entrar();

        definirMeta(sessao, "{\"valor\":0}", 400);
        definirMeta(sessao, "{\"valor\":-5}", 400);
        definirMeta(sessao, "{\"valor\":10.123}", 400);
        definirMeta(sessao, "{\"valor\":100000000}", 400);
        definirMeta(sessao, "{}", 400);
    }

    @Test
    void removeAMeta() throws Exception {
        MockHttpSession sessao = entrar();
        definirMeta(sessao, "{\"valor\":900}", 200);

        mockMvc.perform(delete("/api/usuario/meta").with(csrf()).session(sessao))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuario/meta").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMensal").doesNotExist());
    }

    @Test
    void definirMetaSemTokenCsrfDevolve403() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(put("/api/usuario/meta").session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":900}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void excluirContaComSenhaErradaDevolve403ENaoApaga() throws Exception {
        MockHttpSession sessao = entrar();
        long idNota = salvarNota(sessao);
        Long idUsuario = usuarios.buscarPorEmail(emailLogado).orElseThrow().getId();

        excluirConta(sessao, "senha-errada-999", 403);

        // Continua logado e com a nota
        mockMvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isOk());
        assertTrue(notas.buscarComItens(idNota, idUsuario).isPresent());
    }

    @Test
    void excluirContaApagaTudoDoUsuarioEEncerraASessao() throws Exception {
        MockHttpSession sessao = entrar();
        long idNota = salvarNota(sessao);
        Long idUsuario = usuarios.buscarPorEmail(emailLogado).orElseThrow().getId();

        excluirConta(sessao, SENHA, 204);
        em.flush();
        em.clear();

        // O usuário e a nota dele sumiram
        assertTrue(usuarios.buscarPorEmail(emailLogado).isEmpty());
        assertTrue(notas.buscarComItens(idNota, idUsuario).isEmpty());
        // A sessão foi encerrada e o login não funciona mais
        mockMvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais(emailLogado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void excluirContaNaoMexeNosDadosDeOutroUsuario() throws Exception {
        MockHttpSession sessaoA = entrar();
        salvarNota(sessaoA);
        String emailA = emailLogado;

        MockHttpSession sessaoB = entrar();
        salvarNota(sessaoB);
        Long idB = usuarios.buscarPorEmail(emailLogado).orElseThrow().getId();

        // Entra de novo como A só para excluir a conta dela
        MvcResult loginA = mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais(emailA)))
                .andExpect(status().isOk())
                .andReturn();
        excluirConta((MockHttpSession) loginA.getRequest().getSession(false), SENHA, 204);
        em.flush();
        em.clear();

        // A nota da B continua lá, e o mercado (dado público compartilhado) também
        assertEquals(1, notas.listarDoPeriodo(idB, LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1)).size());
        assertTrue(mercados.findByCnpj("08710596000125").isPresent());
    }
}