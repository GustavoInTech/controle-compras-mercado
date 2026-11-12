package com.gustavointech.compras.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/meta")
    public MetaResponse meta(@AuthenticationPrincipal UsuarioDetalhes usuario) {
        return new MetaResponse(usuarios.meta(usuario.getId()));
    }

    @PutMapping("/meta")
    public MetaResponse definirMeta(@Valid @RequestBody MetaRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return new MetaResponse(usuarios.definirMeta(usuario.getId(), pedido.getValor()));
    }

    @DeleteMapping("/meta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMeta(@AuthenticationPrincipal UsuarioDetalhes usuario) {
        usuarios.definirMeta(usuario.getId(), null);
    }

    // Exclui a conta e todos os dados (LGPD, RF18) e encerra a sessão
    @PostMapping("/excluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirConta(@Valid @RequestBody ExcluirContaRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario,
            HttpServletRequest request) {
        usuarios.excluirConta(usuario.getId(), pedido.getSenha());

        // A conta não existe mais: a sessão também não
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}