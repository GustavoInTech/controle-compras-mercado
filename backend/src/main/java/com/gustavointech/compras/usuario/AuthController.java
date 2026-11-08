package com.gustavointech.compras.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarios;
    private final AutenticacaoService autenticacao;

    public AuthController(UsuarioService usuarios, AutenticacaoService autenticacao) {
        this.usuarios = usuarios;
        this.autenticacao = autenticacao;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest pedido) {
        Usuario criado = usuarios.cadastrar(pedido.getEmail(), pedido.getSenha());
        return new UsuarioResponse(criado.getId(), criado.getEmail());
    }

    @PostMapping("/login")
    public UsuarioResponse entrar(@Valid @RequestBody LoginRequest pedido,
            HttpServletRequest request, HttpServletResponse response) {
        return autenticacao.entrar(pedido.getEmail(), pedido.getSenha(), request, response);
    }

    // Quem está logado agora? (o frontend usa isto ao abrir a página)
    @GetMapping("/eu")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioDetalhes usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername());
    }
}