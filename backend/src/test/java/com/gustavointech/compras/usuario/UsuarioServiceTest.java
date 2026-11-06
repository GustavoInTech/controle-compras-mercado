package com.gustavointech.compras.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

// Usa o banco de desenvolvimento (Docker ligado). @Transactional desfaz tudo ao fim de cada teste.
@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService servico;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@teste.com";
    }

    @Test
    void guardaASenhaEmHashEnuncaEmTextoPuro() {
        Usuario criado = servico.cadastrar(emailNovo(), "senha-forte-123");

        assertNotEquals("senha-forte-123", criado.getSenhaHash());
        assertTrue(criado.getSenhaHash().startsWith("$2"), "deveria ser um hash BCrypt");
        assertTrue(passwordEncoder.matches("senha-forte-123", criado.getSenhaHash()));
        assertTrue(!passwordEncoder.matches("outra-senha", criado.getSenhaHash()));
    }

    @Test
    void guardaOEmailEmMinusculas() {
        String email = emailNovo();

        Usuario criado = servico.cadastrar(email.toUpperCase(), "senha-forte-123");

        assertEquals(email.toLowerCase(), criado.getEmail());
    }

    @Test
    void recusaEmailRepetidoSemDiferenciarMaiusculas() {
        String email = emailNovo();
        servico.cadastrar(email, "senha-forte-123");

        assertThrows(EmailJaCadastradoException.class,
                () -> servico.cadastrar(email.toUpperCase(), "outra-senha-123"));
    }
}