package com.gustavointech.compras.produto;

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
import java.util.List;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.gustavointech.compras.usuario.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Produtos, itens "para revisar" e associação de nomes (ADR 0005), passando pela segurança.
// @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class ProdutoControllerTest {

    private static final String BATATA = "BATATA KG";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private CategoriaRepository categorias;
    @Autowired
    private ProdutoApelidoRepository apelidos;
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

    // Salva uma nota pela API (a data só é necessária na versão do celular)
    private void salvarNota(MockHttpSession sessao, String arquivo, String data) throws Exception {
        String json = "{\"texto\":" + aspas(lerArquivo(arquivo))
                + (data == null ? "" : ",\"data\":\"" + data + "\"") + "}";
        mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    private String pendentes(MockHttpSession sessao) throws Exception {
        return mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private long idCategoria(String nome) {
        return categorias.findAllByOrderByNomeAsc().stream()
                .filter(c -> c.getNome().equals(nome))
                .findFirst().orElseThrow().getId();
    }

    // Pega o valor numérico de um campo no texto JSON (o primeiro que aparecer)
    private static long numero(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\":(\\d+)").matcher(json);
        assertTrue(m.find(), "não achei " + campo + " em: " + json);
        return Long.parseLong(m.group(1));
    }

    private static String jsonProduto(String nome, Long categoriaId, Long mercadoId, String nomeNormalizado) {
        StringBuilder json = new StringBuilder("{\"nome\":" + aspas(nome));
        if (categoriaId != null) {
            json.append(",\"categoriaId\":").append(categoriaId);
        }
        if (mercadoId != null) {
            json.append(",\"mercadoId\":").append(mercadoId);
        }
        if (nomeNormalizado != null) {
            json.append(",\"nomeNormalizado\":").append(aspas(nomeNormalizado));
        }
        return json.append("}").toString();
    }

    private ResultActions criarProduto(MockHttpSession sessao, String json) throws Exception {
        return mockMvc.perform(post("/api/produtos").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // Cria um produto (com apelido, se mercadoId e nomeNormalizado vierem) e
    // devolve o id dele
    private long criarProdutoOk(MockHttpSession sessao, String nome, String categoria,
            Long mercadoId, String nomeNormalizado) throws Exception {
        String corpo = criarProduto(sessao, jsonProduto(nome, idCategoria(categoria), mercadoId, nomeNormalizado))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return numero(corpo, "id");
    }

    private ResultActions associar(MockHttpSession sessao, long produtoId, long mercadoId, String nome)
            throws Exception {
        return mockMvc.perform(post("/api/produtos/" + produtoId + "/apelidos").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mercadoId\":" + mercadoId + ",\"nomeNormalizado\":\"" + nome + "\"}"));
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isUnauthorized());
    }

    @Test
    void listaAsCategoriasEmOrdemAlfabetica() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/categorias").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].nome").value("Bebidas"));
    }

    @Test
    void pendentesListaOsItensSemProduto() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].mercado").value("ACOUGUE E MERC AREAL DE RIO DAS PEDRAS LTDA-ME"))
                .andExpect(jsonPath("$[0].nomeNormalizado").value("ASA FRANGO CG COPAVEL KG"))
                .andExpect(jsonPath("$[0].ocorrencias").value(1))
                .andExpect(jsonPath("$[0].ultimaCompra").value("2026-10-25"));
    }

    @Test
    void criaProdutoJaLigandoOApelido() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);
        long mercadoId = numero(pendentes(sessao), "mercadoId");

        criarProduto(sessao, jsonProduto("Batata", idCategoria("Hortifruti"), mercadoId, BATATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Batata"))
                .andExpect(jsonPath("$.categoria").value("Hortifruti"));

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$.length()").value(9));
        mockMvc.perform(get("/api/produtos").session(sessao))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Batata"));
    }

    @Test
    void aLigacaoAlcancaTodasAsNotasDoMesmoNome() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null); // 25/10
        salvarNota(sessao, "acougue-celular.txt", "2026-10-26"); // outra compra, no mesmo mercado
        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].ocorrencias").value(2));
        long mercadoId = numero(pendentes(sessao), "mercadoId");
        long produtoId = criarProdutoOk(sessao, "Batata", "Hortifruti", null, null);

        // As duas linhas "BATATA KG" (uma de cada nota) são ligadas de uma vez
        associar(sessao, produtoId, mercadoId, BATATA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itensLigados").value(2));

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$.length()").value(9));
    }

    @Test
    void notaNovaLigaSozinhaOsNomesJaConfirmados() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);
        long mercadoId = numero(pendentes(sessao), "mercadoId");
        criarProdutoOk(sessao, "Batata", "Hortifruti", mercadoId, BATATA);

        // A segunda compra traz "BATATA KG" de novo: o sistema liga sozinho, sem pedir
        // revisão
        salvarNota(sessao, "acougue-celular.txt", "2026-10-26");

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')]").isEmpty())
                .andExpect(jsonPath("$[0].ocorrencias").value(2));
    }

    @Test
    void corrigirAAssociacaoReligaOsItensEMantemUmSoApelido() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);
        long mercadoId = numero(pendentes(sessao), "mercadoId");
        criarProdutoOk(sessao, "Batata", "Hortifruti", mercadoId, BATATA);
        long produtoB = criarProdutoOk(sessao, "Batata inglesa", "Hortifruti", null, null);

        associar(sessao, produtoB, mercadoId, BATATA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itensLigados").value(1));

        Long idUsuario = usuarios.buscarPorEmail(emailLogado).orElseThrow().getId();
        List<ProdutoApelido> lista = apelidos.findByUsuarioIdAndMercadoId(idUsuario, mercadoId);
        assertEquals(1, lista.size(), "o apelido deveria ser corrigido, e não duplicado");
        assertEquals(produtoB, lista.get(0).getProduto().getId().longValue());
    }

    @Test
    void excluirOProdutoDevolveOsItensParaRevisar() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);
        long mercadoId = numero(pendentes(sessao), "mercadoId");
        long produtoId = criarProdutoOk(sessao, "Batata", "Hortifruti", mercadoId, BATATA);
        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$.length()").value(9));

        mockMvc.perform(delete("/api/produtos/" + produtoId).with(csrf()).session(sessao))
                .andExpect(status().isNoContent());
        em.flush();
        em.clear();

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$.length()").value(10));
        mockMvc.perform(get("/api/produtos").session(sessao))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void usuarioNaoMexeNosProdutosDeOutro() throws Exception {
        MockHttpSession dono = entrar();
        salvarNota(dono, "acougue-computador.txt", null);
        long mercadoId = numero(pendentes(dono), "mercadoId");
        long produtoId = criarProdutoOk(dono, "Batata", "Hortifruti", mercadoId, BATATA);

        MockHttpSession intruso = entrar();

        mockMvc.perform(get("/api/produtos").session(intruso)).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/produtos/pendentes").session(intruso)).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(put("/api/produtos/" + produtoId).with(csrf()).session(intruso)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Roubado\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/produtos/" + produtoId).with(csrf()).session(intruso))
                .andExpect(status().isNotFound());
        associar(intruso, produtoId, mercadoId, BATATA).andExpect(status().isNotFound());
    }

    @Test
    void atualizaOProdutoERecusaCategoriaInexistente() throws Exception {
        MockHttpSession sessao = entrar();
        long produtoId = criarProdutoOk(sessao, "Batata", "Hortifruti", null, null);

        mockMvc.perform(put("/api/produtos/" + produtoId).with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonProduto("Batata inglesa", idCategoria("Carnes"), null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Batata inglesa"))
                .andExpect(jsonPath("$.categoria").value("Carnes"));

        mockMvc.perform(put("/api/produtos/" + produtoId).with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonProduto("Batata", 999999999L, null, null)))
                .andExpect(status().is(422));
    }

    @Test
    void apelidoIncompletoDevolve400EMercadoInexistenteDevolve422SemCriarNada() throws Exception {
        MockHttpSession sessao = entrar();

        // mercadoId sem nomeNormalizado: os dois vão juntos ou nenhum
        criarProduto(sessao, jsonProduto("Batata", null, 1L, null)).andExpect(status().isBadRequest());
        // mercado que não existe
        criarProduto(sessao, jsonProduto("Batata", null, 999999999L, BATATA)).andExpect(status().is(422));

        mockMvc.perform(get("/api/produtos").session(sessao)).andExpect(jsonPath("$.length()").value(0));
    }
}