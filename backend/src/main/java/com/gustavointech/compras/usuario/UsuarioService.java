package com.gustavointech.compras.usuario;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(String email, String senha) {
        if (usuarios.buscarPorEmail(email.strip()).isPresent()) {
            throw new EmailJaCadastradoException();
        }
        Usuario novo = new Usuario(email, passwordEncoder.encode(senha));
        try {
            return usuarios.saveAndFlush(novo);
        } catch (DataIntegrityViolationException e) {
            // Dois cadastros ao mesmo tempo com o mesmo e-mail: quem decide é o índice
            // único do banco
            throw new EmailJaCadastradoException();
        }
    }
}