package com.gustavointech.compras.nota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.usuario.Usuario;
import com.gustavointech.compras.usuario.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Usa o banco de desenvolvimento (Docker ligado). @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class NotaRepositoryTest {

    @Autowired
    private NotaRepository notas;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private MercadoRepository mercados;
    @PersistenceContext
    private EntityManager em;

    // E-mail aleatório: o teste nunca esbarra num usuário que já exista no banco
    private Usuario novoUsuario() {
        return usuarios.saveAndFlush(new Usuario("teste-" + UUID.randomUUID() + "@teste.com", "hash"));
    }

    private Mercado novoMercado() {
        return mercados.saveAndFlush(new Mercado("99999999999999", "Mercado de Teste", "Rua do Teste, 1"));
    }

    private Nota novaNota(Usuario usuario, Mercado mercado, String identificador, LocalDate data) {
        Nota nota = new Nota(usuario, mercado, identificador, null, data,
                new BigDecimal("5.32"), BigDecimal.ZERO, new BigDecimal("5.32"));
        nota.adicionarItem(new ItemNota("Beterraba kg", "BETERRABA KG", "8885",
                new BigDecimal("0.245"), "KG", new BigDecimal("3.99"), new BigDecimal("0.97")));
        nota.adicionarItem(new ItemNota("Cebola kg", "CEBOLA KG", "8886",
                new BigDecimal("0.415"), "KG", new BigDecimal("10.49"), new BigDecimal("4.35")));
        return nota;
    }

    private long contarItens(Long notaId) {
        return em.createQuery("select count(i) from ItemNota i where i.nota.id = :id", Long.class)
                .setParameter("id", notaId).getSingleResult();
    }

    @Test
    void salvaNotaComItensEBuscaDeVoltaComOsItens() {
        Usuario usuario = novoUsuario();
        Nota salva = notas.saveAndFlush(novaNota(usuario, novoMercado(), "id-1", LocalDate.of(2026, 10, 25)));
        em.clear(); // esquece o que está na memória: a próxima busca vai mesmo ao banco

        Nota lida = notas.buscarComItens(salva.getId(), usuario.getId()).orElseThrow();

        assertEquals(2, lida.getItens().size());
        assertEquals("Beterraba kg", lida.getItens().get(0).getNomeOriginal());
        assertEquals("Mercado de Teste", lida.getMercado().getNome());
        assertNotNull(lida.getCriadoEm());
    }

    @Test
    void naoPermiteAMesmaNotaDuasVezesParaOMesmoUsuario() {
        Usuario usuario = novoUsuario();
        Mercado mercado = novoMercado();
        notas.saveAndFlush(novaNota(usuario, mercado, "id-1", LocalDate.of(2026, 10, 25)));

        assertThrows(DataIntegrityViolationException.class,
                () -> notas.saveAndFlush(novaNota(usuario, mercado, "id-1", LocalDate.of(2026, 10, 25))));
    }

    @Test
    void outroUsuarioPodeRegistrarAMesmaNota() {
        Mercado mercado = novoMercado();
        notas.saveAndFlush(novaNota(novoUsuario(), mercado, "id-1", LocalDate.of(2026, 10, 25)));

        Nota outra = notas.saveAndFlush(novaNota(novoUsuario(), mercado, "id-1", LocalDate.of(2026, 10, 25)));

        assertNotNull(outra.getId());
    }

    @Test
    void sabeSeANotaJaFoiRegistradaPeloUsuario() {
        Usuario dono = novoUsuario();
        Usuario outro = novoUsuario();
        notas.saveAndFlush(novaNota(dono, novoMercado(), "id-1", LocalDate.of(2026, 10, 25)));

        assertTrue(notas.existsByUsuarioIdAndIdentificador(dono.getId(), "id-1"));
        assertFalse(notas.existsByUsuarioIdAndIdentificador(outro.getId(), "id-1"));
        assertFalse(notas.existsByUsuarioIdAndIdentificador(dono.getId(), "outro-id"));
    }

    @Test
    void usuarioNaoEnxergaANotaDeOutro() {
        Usuario dono = novoUsuario();
        Usuario intruso = novoUsuario();
        Nota nota = notas.saveAndFlush(novaNota(dono, novoMercado(), "id-1", LocalDate.of(2026, 10, 25)));
        em.clear();

        assertTrue(notas.buscarComItens(nota.getId(), dono.getId()).isPresent());
        assertTrue(notas.buscarComItens(nota.getId(), intruso.getId()).isEmpty());
    }

    @Test
    void listaSoAsNotasDoUsuarioNoPeriodo() {
        Usuario usuario = novoUsuario();
        Mercado mercado = novoMercado();
        notas.saveAndFlush(novaNota(usuario, mercado, "outubro", LocalDate.of(2026, 10, 25)));
        notas.saveAndFlush(novaNota(usuario, mercado, "setembro", LocalDate.of(2026, 9, 30)));
        notas.saveAndFlush(novaNota(novoUsuario(), mercado, "de-outro", LocalDate.of(2026, 10, 25)));
        em.clear();

        List<Nota> doMes = notas.listarDoPeriodo(usuario.getId(), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1));

        assertEquals(1, doMes.size());
        assertEquals("outubro", doMes.get(0).getIdentificador());
    }

    @Test
    void apagarANotaApagaOsItens() {
        Nota nota = notas.saveAndFlush(novaNota(novoUsuario(), novoMercado(), "id-1", LocalDate.of(2026, 10, 25)));
        Long id = nota.getId();
        assertEquals(2L, contarItens(id));

        notas.delete(nota);
        notas.flush();
        em.clear();

        assertEquals(0L, contarItens(id));
    }
}