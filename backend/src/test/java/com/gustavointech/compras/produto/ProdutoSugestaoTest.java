package com.gustavointech.compras.produto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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

// As sugestões da lista "para revisar" (ADR 0005, decisão 4), de ponta a ponta, com o banco de desenvolvimento
// (que precisa ter a migração V3, da extensão pg_trgm). @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class ProdutoSugestaoTest {

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private CategoriaRepository categorias;

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

    // A nota do atacadista, com um dos itens trocado por outro nome (o valor do
    // item continua igual,
    // então a nota continua conferindo)
    private String atacadistaCom(String itemNovo) throws IOException {
        return lerArquivo("atacadista.txt").replace("SAC ASSA VD BI 48X55 (Código: 1202652 )", itemNovo);
    }

    // Transforma um texto em um valor JSON (com aspas, \n, \t etc.)
    private static String aspas(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    // Cadastra um usuário novo, faz login e devolve a sessão
    private MockHttpSession entrar() throws Exception {
        String credenciais = "{\"email\":\"teste-" + UUID.randomUUID() + "@teste.com\",\"senha\":\"senha-forte-123\"}";

        mockMvc.perform(post("/api/auth/cadastro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credenciais))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    private void salvarTexto(MockHttpSession sessao, String texto) throws Exception {
        mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":" + aspas(texto) + "}"))
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

    private static long numero(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\":(\\d+)").matcher(json);
        assertTrue(m.find(), "não achei " + campo + " em: " + json);
        return Long.parseLong(m.group(1));
    }

    // Cria o produto já ligando a ele um nome de uma loja (como a tela "para
    // revisar" faria)
    private void criarProdutoComApelido(MockHttpSession sessao, String nome, String categoria,
            long mercadoId, String nomeNormalizado) throws Exception {
        String json = "{\"nome\":" + aspas(nome) + ",\"categoriaId\":" + idCategoria(categoria)
                + ",\"mercadoId\":" + mercadoId + ",\"nomeNormalizado\":" + aspas(nomeNormalizado) + "}";
        mockMvc.perform(post("/api/produtos").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void oDicionarioSugereACategoriaDosNomesDaNota() throws Exception {
        MockHttpSession sessao = entrar();
        salvarTexto(sessao, lerArquivo("acougue-computador.txt"));

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoCategoria.nome").value("Hortifruti"))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoCategoria.origem").value("DICIONARIO"))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'CONTR BOVINO KG')].sugestaoCategoria.nome").value("Carnes"))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'CARVA VEGETAL ITOROR 3KG')].sugestaoCategoria.nome")
                        .value("Outros"))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'CARVA VEGETAL ITOROR 3KG')].sugestaoCategoria.origem")
                        .value("PADRAO"))
                // Sem nenhum produto confirmado, não há o que sugerir como produto
                .andExpect(jsonPath("$[0].sugestaoProduto").doesNotExist());
    }

    @Test
    void oMesmoNomeEmOutraLojaSugereOProdutoComConfiancaAlta() throws Exception {
        MockHttpSession sessao = entrar();
        salvarTexto(sessao, lerArquivo("acougue-computador.txt"));
        long mercadoAreal = numero(pendentes(sessao), "mercadoId");
        criarProdutoComApelido(sessao, "Batata", "Hortifruti", mercadoAreal, "BATATA KG");

        // Outra loja (o atacadista) com um item que também se chama "Batata kg"
        salvarTexto(sessao, atacadistaCom("Batata kg (Código: 8885 )"));

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoProduto.nome").value("Batata"))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoProduto.confianca").value("ALTA"))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoCategoria.origem").value("MESMO_NOME"))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA KG')].sugestaoCategoria.nome").value("Hortifruti"));
    }

    @Test
    void umNomeParecidoSugereOProdutoComConfiancaMedia() throws Exception {
        MockHttpSession sessao = entrar();
        salvarTexto(sessao, lerArquivo("acougue-computador.txt"));
        long mercadoAreal = numero(pendentes(sessao), "mercadoId");
        criarProdutoComApelido(sessao, "Batata", "Hortifruti", mercadoAreal, "BATATA KG");

        salvarTexto(sessao, atacadistaCom("Batata inglesa kg (Código: 8885 )"));

        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA INGLESA KG')].sugestaoProduto.nome").value("Batata"))
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'BATATA INGLESA KG')].sugestaoProduto.confianca")
                        .value("MEDIA"))
                // Não é o mesmo nome: a categoria vem do dicionário, e não do produto
                .andExpect(jsonPath("$[?(@.nomeNormalizado == 'BATATA INGLESA KG')].sugestaoCategoria.origem")
                        .value("DICIONARIO"));
    }

    @Test
    void nomesDiferentesQueSoCompartilhamOKgNaoViramSugestao() throws Exception {
        MockHttpSession sessao = entrar();
        salvarTexto(sessao, lerArquivo("acougue-computador.txt"));
        long mercadoAreal = numero(pendentes(sessao), "mercadoId");
        criarProdutoComApelido(sessao, "Cebola", "Hortifruti", mercadoAreal, "CEBOLA KG");

        // CENOURA KG parece com CEBOLA KG só pelo "KG" (similaridade 0,31): não pode
        // virar sugestão
        mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'CENOURA KG' && @.sugestaoProduto == null)].nomeNormalizado")
                                .value("CENOURA KG"));
    }

    @Test
    void asSugestoesSoUsamOsProdutosDoProprioUsuario() throws Exception {
        MockHttpSession dono = entrar();
        salvarTexto(dono, lerArquivo("acougue-computador.txt"));
        long mercadoAreal = numero(pendentes(dono), "mercadoId");
        criarProdutoComApelido(dono, "Batata", "Hortifruti", mercadoAreal, "BATATA KG");

        // Outro usuário registra a mesma compra: o produto do dono não pode aparecer
        // como sugestão para ele
        MockHttpSession outro = entrar();
        salvarTexto(outro, lerArquivo("acougue-computador.txt"));

        mockMvc.perform(get("/api/produtos/pendentes").session(outro))
                .andExpect(
                        jsonPath("$[?(@.nomeNormalizado == 'BATATA KG' && @.sugestaoProduto == null)].nomeNormalizado")
                                .value("BATATA KG"));
    }
}