package com.gustavointech.compras.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

// Login de verdade: cadastro, login, sessão, "quem sou eu" e logout, passando pelos filtros de segurança.
@SpringBootTest
@Transactional
class LoginTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void montar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@teste.com";
    }

    private static String json(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    private void cadastrar(String email, String senha) throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json(email, senha)))
                .andExpect(status().isCreated());
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json(email, senha)));
    }

    private static MockHttpSession sessaoDe(MvcResult resultado) {
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    @Test
    void loginComSenhaCertaDevolve200ECriaSessao() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");

        MvcResult resultado = login(email, "senha-forte-123")
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(sessaoDe(resultado), "o login deveria criar uma sessão");
        String corpo = resultado.getResponse().getContentAsString();
        assertTrue(corpo.contains(email));
        assertFalse(corpo.toLowerCase().contains("senha"), "a resposta não pode ter a senha: " + corpo);
        assertFalse(corpo.toLowerCase().contains("hash"), "a resposta não pode ter o hash: " + corpo);
    }

    @Test
    void euDevolveOUsuarioLogado() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");
        MockHttpSession sessao = sessaoDe(login(email, "senha-forte-123").andReturn());

        String corpo = mockMvc.perform(get("/api/auth/eu").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(corpo.contains(email));
    }

    @Test
    void euSemLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
    }

    @Test
    void senhaErradaDevolve401() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");

        login(email, "senha-errada-999").andExpect(status().isUnauthorized());
    }

    @Test
    void emailInexistenteDevolveAMesmaRespostaDeSenhaErrada() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");

        String comSenhaErrada = login(email, "senha-errada-999")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String semUsuario = login(emailNovo(), "senha-errada-999")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertEquals(comSenhaErrada, semUsuario, "as respostas não podem revelar se o e-mail existe");
    }

    @Test
    void loginAceitaOEmailEmOutraCaixa() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");

        login(email.toUpperCase(), "senha-forte-123").andExpect(status().isOk());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        String email = emailNovo();
        cadastrar(email, "senha-forte-123");
        MockHttpSession sessao = sessaoDe(login(email, "senha-forte-123").andReturn());

        mockMvc.perform(post("/api/auth/logout").with(csrf()).session(sessao))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/eu").session(sessao))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSemTokenCsrfDevolve403() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(json("a@teste.com", "senha-forte-123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginComCorpoVazioDevolve400() throws Exception {
        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}