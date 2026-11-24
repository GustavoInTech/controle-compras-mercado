package com.gustavointech.compras.relatorio;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.gustavointech.compras.produto.CategoriaRepository;

// O dashboard do mês de ponta a ponta, com notas reais (anonimizadas) salvas pela própria API.
// @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class RelatorioControllerTest {

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

    // Salva uma nota pela API (a data só é necessária na versão do celular)
    private void salvarNota(MockHttpSession sessao, String arquivo, String data) throws Exception {
        String json = "{\"texto\":" + aspas(lerArquivo(arquivo))
                + (data == null ? "" : ",\"data\":\"" + data + "\"") + "}";
        mockMvc.perform(post("/api/notas").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
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

    private void definirMeta(MockHttpSession sessao, String valor) throws Exception {
        mockMvc.perform(put("/api/usuario/meta").with(csrf()).session(sessao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":" + valor + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void semLoginDevolve401() throws Exception {
        mockMvc.perform(get("/api/relatorios/mes")).andExpect(status().isUnauthorized());
    }

    @Test
    void mesSemNotasDevolveZerosSemPercentualNemMeta() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "5").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano").value(2026))
                .andExpect(jsonPath("$.mes").value(5))
                .andExpect(jsonPath("$.gastoTotal").value(0.0))
                .andExpect(jsonPath("$.quantidadeNotas").value(0))
                .andExpect(jsonPath("$.descontos").value(0.0))
                .andExpect(jsonPath("$.valorDosItens").value(0.0))
                .andExpect(jsonPath("$.porCategoria.length()").value(0))
                .andExpect(jsonPath("$.mesAnterior.mes").value(4))
                .andExpect(jsonPath("$.mesAnterior.variacaoPercentual").doesNotExist())
                .andExpect(jsonPath("$.meta").doesNotExist());
    }

    @Test
    void gastoTotalUsaOValorPagoEAContaFechaComOsDescontos() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null); // 25/10: itens 196,80, sem desconto
        salvarNota(sessao, "atacadista.txt", null); // 03/10: itens 589,91, desconto 16,51, pago 573,40

        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gastoTotal").value(770.2)) // 196,80 + 573,40
                .andExpect(jsonPath("$.quantidadeNotas").value(2))
                .andExpect(jsonPath("$.descontos").value(16.51))
                .andExpect(jsonPath("$.valorDosItens").value(786.71)) // 786,71 - 16,51 = 770,20
                // Nenhum produto foi revisado ainda: tudo fica em "Sem categoria"
                .andExpect(jsonPath("$.porCategoria.length()").value(1))
                .andExpect(jsonPath("$.porCategoria[0].categoria").value("Sem categoria"))
                .andExpect(jsonPath("$.porCategoria[0].gasto").value(786.71))
                .andExpect(jsonPath("$.porCategoria[0].percentual").value(100.0))
                // Sem notas em setembro: a variação em reais existe, em % não
                .andExpect(jsonPath("$.mesAnterior.gastoTotal").value(0.0))
                .andExpect(jsonPath("$.mesAnterior.variacao").value(770.2))
                .andExpect(jsonPath("$.mesAnterior.variacaoPercentual").doesNotExist())
                .andExpect(jsonPath("$.meta").doesNotExist());
    }

    @Test
    void gastoPorCategoriaUsaOsProdutosRevisados() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-computador.txt", null);
        String pendentes = mockMvc.perform(get("/api/produtos/pendentes").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long mercadoId = numero(pendentes, "mercadoId");
        criarProdutoComApelido(sessao, "Batata", "Hortifruti", mercadoId, "BATATA KG"); // 4,92
        criarProdutoComApelido(sessao, "Contrafilé", "Carnes", mercadoId, "CONTR BOVINO KG"); // 72,82

        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gastoTotal").value(196.8))
                .andExpect(jsonPath("$.valorDosItens").value(196.8))
                .andExpect(jsonPath("$.porCategoria.length()").value(3))
                .andExpect(jsonPath("$.porCategoria[0].categoria").value("Sem categoria"))
                .andExpect(jsonPath("$.porCategoria[0].gasto").value(119.06))
                .andExpect(jsonPath("$.porCategoria[0].percentual").value(60.5))
                .andExpect(jsonPath("$.porCategoria[1].categoria").value("Carnes"))
                .andExpect(jsonPath("$.porCategoria[1].gasto").value(72.82))
                .andExpect(jsonPath("$.porCategoria[1].percentual").value(37.0))
                .andExpect(jsonPath("$.porCategoria[2].categoria").value("Hortifruti"))
                .andExpect(jsonPath("$.porCategoria[2].gasto").value(4.92))
                .andExpect(jsonPath("$.porCategoria[2].percentual").value(2.5));
    }

    @Test
    void comparaComOMesAnterior() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "acougue-celular.txt", "2026-09-20"); // setembro: 196,80
        salvarNota(sessao, "atacadista.txt", null); // outubro: 573,40

        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gastoTotal").value(573.4))
                .andExpect(jsonPath("$.mesAnterior.ano").value(2026))
                .andExpect(jsonPath("$.mesAnterior.mes").value(9))
                .andExpect(jsonPath("$.mesAnterior.gastoTotal").value(196.8))
                .andExpect(jsonPath("$.mesAnterior.quantidadeNotas").value(1))
                .andExpect(jsonPath("$.mesAnterior.variacao").value(376.6))
                .andExpect(jsonPath("$.mesAnterior.variacaoPercentual").value(191.4));
    }

    @Test
    void mostraOProgressoDaMetaMensal() throws Exception {
        MockHttpSession sessao = entrar();
        salvarNota(sessao, "atacadista.txt", null); // pago 573,40

        definirMeta(sessao, "1000");
        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(jsonPath("$.meta.valor").value(1000.0))
                .andExpect(jsonPath("$.meta.percentualUsado").value(57.3))
                .andExpect(jsonPath("$.meta.restante").value(426.6))
                .andExpect(jsonPath("$.meta.ultrapassada").value(false));

        // Com uma meta menor, ela é ultrapassada
        definirMeta(sessao, "500");
        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(sessao))
                .andExpect(jsonPath("$.meta.percentualUsado").value(114.7))
                .andExpect(jsonPath("$.meta.restante").value(-73.4))
                .andExpect(jsonPath("$.meta.ultrapassada").value(true));
    }

    @Test
    void outroUsuarioNaoEnxergaOsGastosDeOutro() throws Exception {
        MockHttpSession dono = entrar();
        salvarNota(dono, "acougue-computador.txt", null);

        MockHttpSession outro = entrar();
        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "10").session(outro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gastoTotal").value(0.0))
                .andExpect(jsonPath("$.quantidadeNotas").value(0))
                .andExpect(jsonPath("$.porCategoria.length()").value(0));
    }

    @Test
    void mesInvalidoOuIncompletoDevolve400() throws Exception {
        MockHttpSession sessao = entrar();

        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").param("mes", "13").session(sessao))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/relatorios/mes").param("ano", "2026").session(sessao))
                .andExpect(status().isBadRequest());
    }
}