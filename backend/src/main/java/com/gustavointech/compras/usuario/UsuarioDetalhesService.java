package com.gustavointech.compras.usuario;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// O Spring Security usa esta classe para buscar o usuário pelo e-mail durante o login
@Service
public class UsuarioDetalhesService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetalhesService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarios.buscarPorEmail(email)
                .map(UsuarioDetalhes::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}