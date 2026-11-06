package com.gustavointech.compras.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

// Usa o banco de desenvolvimento (o Docker precisa estar ligado).
// @Transactional desfaz tudo ao final de cada teste, então nada fica gravado.
@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repositorio;

    @Test
    void salvaEBuscaPorEmailSemDiferenciarMaiusculas() {
        Usuario salvo = repositorio.saveAndFlush(new Usuario("Ana@Email.com", "hash"));

        assertNotNull(salvo.getId());
        assertNotNull(salvo.getCriadoEm());

        Optional<Usuario> achado = repositorio.buscarPorEmail("ANA@email.COM");
        assertTrue(achado.isPresent());
        assertEquals("ana@email.com", achado.get().getEmail());
    }

    @Test
    void naoPermiteDoisUsuariosComOMesmoEmail() {
        repositorio.saveAndFlush(new Usuario("bia@email.com", "hash"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repositorio.saveAndFlush(new Usuario("BIA@email.com", "hash")));
    }
}