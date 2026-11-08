package com.gustavointech.compras.usuario;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// Confere a regra geral de segurança: o que é aberto, o que exige login e o CSRF.
@SpringBootTest
class SegurancaTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void montar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void saudeEAberta() throws Exception {
        mockMvc.perform(get("/api/saude"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void rotaProtegidaSemLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/notas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pedidoQueAlteraDadosSemTokenCsrfDevolve403() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"sem-token@teste.com\",\"senha\":\"senha-forte-123\"}"))
                .andExpect(status().isForbidden());
    }
}