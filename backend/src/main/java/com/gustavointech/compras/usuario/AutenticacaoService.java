package com.gustavointech.compras.usuario;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    // Guarda "quem está logado" na sessão do servidor
    private final SecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    public AutenticacaoService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public UsuarioResponse entrar(String email, String senha,
            HttpServletRequest request, HttpServletResponse response) {
        // Lança AuthenticationException se o e-mail não existir ou a senha estiver
        // errada
        Authentication autenticado = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email.strip(), senha));

        // Evita "fixação de sessão": se já havia uma sessão antes do login, troca o
        // identificador dela
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        repositorio.saveContext(contexto, request, response);

        UsuarioDetalhes principal = (UsuarioDetalhes) autenticado.getPrincipal();
        return new UsuarioResponse(principal.getId(), principal.getUsername());
    }
}