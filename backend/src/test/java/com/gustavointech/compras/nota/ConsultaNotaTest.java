package com.gustavointech.compras.nota;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

// Listar, detalhar e excluir de verdade: usuário logado, notas reais (anonimizadas) salvas pela própria API.
@SpringBootTest
@Transactional
class ConsultaNotaTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

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
        String email = "teste-" + UUID.randomUUID() + "@teste.com";
        String credenciais = "{\"email\":\"" + email + "\",\"senha\":\"senha-forte-123\"}";

        mockMvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    // Salva uma nota pela API e devolve o id dela
    private long salvarNota(MockHttpSession sessao, String texto) throws Exception {
        String corpo = mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":" + aspas(texto) + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(corpo);
        assertTrue(m.find(), "a resposta deveria ter o id: " + corpo);
        return Long.parseLong(m.group(1));
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/notas")).andExpect(status().isUnauthorized());
    }

    @Test
    void listaAsNotasDoMesDaMaisNovaParaAMaisAntiga() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, lerArquivo("atacadista.txt")); // 03/10/2026
        salvarNota(sessao, lerArquivo("acougue-computador.txt")); // 25/10/2026

        mockMvc.perform(get("/api/notas").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].mercado").value("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME"))
                .andExpect(jsonPath("$[0].quantidadeItens").value(10))
                .andExpect(jsonPath("$[1].mercado").value("SENDAS DISTRIBUIDORA S/A"))
                .andExpect(jsonPath("$[1].quantidadeItens").value(63))
                .andExpect(jsonPath("$[1].desconto").value(16.51));
    }

    @Test
    void naoListaNotasDeOutroMes() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, lerArquivo("acougue-computador.txt")); // 25/10/2026

        mockMvc.perform(get("/api/notas").param("ano", "2026").param("mes", "9").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void naoListaNotasDeOutroUsuario() throws Exception {
        MockHttpSession dono = entrar();
        salvarNota(dono, lerArquivo("acougue-computador.txt"));

        MockHttpSession outro = entrar();
        mockMvc.perform(get("/api/notas").param("ano", "2026").param("mes", "10").session(outro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void detalhaANotaComOsItens() throws Exception {
        MockHttpSession sessao = entrar();
        long id = salvarNota(sessao, lerArquivo("acougue-computador.txt"));

        mockMvc.perform(get("/api/notas/" + id).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mercado").value("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME"))
                .andExpect(jsonPath("$.cnpj").value("08710596000125"))
                .andExpect(jsonPath("$.valorAPagar").value(196.8))
                .andExpect(jsonPath("$.itens.length()").value(10))
                .andExpect(jsonPath("$.itens[0].nome").value("Beterraba kg"))
                .andExpect(jsonPath("$.itens[0].total").value(0.97));
    }

    @Test
    void notaDeOutroUsuarioDevolve404() throws Exception {
        MockHttpSession dono = entrar();
        long id = salvarNota(dono, lerArquivo("acougue-computador.txt"));

        MockHttpSession intruso = entrar();
        mockMvc.perform(get("/api/notas/" + id).session(intruso)).andExpect(status().isNotFound());
    }

    @Test
    void notaInexistenteDevolve404() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/notas/999999999").session(sessao)).andExpect(status().isNotFound());
    }

    @Test
    void excluiANotaEElaNaoExisteMais() throws Exception {
        MockHttpSession sessao = entrar();
        long id = salvarNota(sessao, lerArquivo("acougue-computador.txt"));

        mockMvc.perform(delete("/api/notas/" + id).with(csrf()).session(sessao))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notas/" + id).session(sessao)).andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiANotaDeOutroUsuario() throws Exception {
        MockHttpSession dono = entrar();
        long id = salvarNota(dono, lerArquivo("acougue-computador.txt"));

        MockHttpSession intruso = entrar();
        mockMvc.perform(delete("/api/notas/" + id).with(csrf()).session(intruso))
                .andExpect(status().isNotFound());

        // A nota continua lá, para o dono
        mockMvc.perform(get("/api/notas/" + id).session(dono)).andExpect(status().isOk());
    }

    @Test
    void excluirSemTokenCsrfDevolve403() throws Exception {
        MockHttpSession sessao = entrar();
        long id = salvarNota(sessao, lerArquivo("acougue-computador.txt"));

        mockMvc.perform(delete("/api/notas/" + id).session(sessao)).andExpect(status().isForbidden());
    }

    @Test
    void mesInvalidoDevolve400() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/notas").param("ano", "2026").param("mes", "13").session(sessao))
                .andExpect(status().isBadRequest());
    }
}