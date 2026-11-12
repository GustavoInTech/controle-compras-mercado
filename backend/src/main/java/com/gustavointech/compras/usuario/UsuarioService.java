package com.gustavointech.compras.usuario;

import java.math.BigDecimal;

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

    // A meta mensal de gasto; null quando o usuário não definiu uma
    @Transactional(readOnly = true)
    public BigDecimal meta(Long usuarioId) {
        return usuarios.findById(usuarioId).orElseThrow().getMetaMensal();
    }

    // Define a meta; com null, remove. O Hibernate grava a mudança ao final da
    // transação.
    @Transactional
    public BigDecimal definirMeta(Long usuarioId, BigDecimal valor) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow();
        usuario.setMetaMensal(valor);
        return usuario.getMetaMensal();
    }

    // Exclui a conta e todos os dados dela (RF18). Pede a senha: uma sessão aberta
    // sozinha não basta.
    @Transactional
    public void excluirConta(Long usuarioId, String senha) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow();
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new SenhaIncorretaException();
        }
        usuarios.excluirPorId(usuarioId);
    }
}