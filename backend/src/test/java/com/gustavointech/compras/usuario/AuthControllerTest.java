package com.gustavointech.compras.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

// Chama o endpoint de verdade (sem abrir uma porta), com o banco de desenvolvimento.
@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void montar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private ResultActions cadastrar(String json) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static String json(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@teste.com";
    }

    @Test
    void cadastraEDevolve201SemExporSenhaNemHash() throws Exception {
        String email = emailNovo();

        String corpo = cadastrar(json(email, "senha-forte-123"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertTrue(corpo.contains(email));
        assertFalse(corpo.toLowerCase().contains("senha"), "a resposta não pode ter a senha: " + corpo);
        assertFalse(corpo.toLowerCase().contains("hash"), "a resposta não pode ter o hash: " + corpo);
    }

    @Test
    void devolve409QuandoOEmailJaExiste() throws Exception {
        String email = emailNovo();
        cadastrar(json(email, "senha-forte-123")).andExpect(status().isCreated());

        cadastrar(json(email, "outra-senha-123")).andExpect(status().isConflict());
    }

    @Test
    void devolve400QuandoASenhaECurta() throws Exception {
        cadastrar(json(emailNovo(), "curta")).andExpect(status().isBadRequest());
    }

    @Test
    void devolve400QuandoOEmailEInvalido() throws Exception {
        cadastrar(json("isto-nao-e-email", "senha-forte-123")).andExpect(status().isBadRequest());
    }

    @Test
    void devolve400QuandoOCorpoEstaVazio() throws Exception {
        cadastrar("{}").andExpect(status().isBadRequest());
    }
}