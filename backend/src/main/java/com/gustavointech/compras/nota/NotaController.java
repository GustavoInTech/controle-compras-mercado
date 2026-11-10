package com.gustavointech.compras.nota;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gustavointech.compras.usuario.UsuarioDetalhes;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final PreviaNotaService previa;
    private final SalvarNotaService salvar;

    public NotaController(PreviaNotaService previa, SalvarNotaService salvar) {
        this.previa = previa;
        this.salvar = salvar;
    }

    // Só lê e confere: não grava nada. Exige login (regra geral do
    // SegurancaConfig).
    @PostMapping("/previa")
    public PreviaNotaResponse previa(@Valid @RequestBody PreviaNotaRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return previa.gerar(usuario.getId(), pedido.getTexto(), pedido.getLink());
    }

    // Lê, confere e GRAVA a nota do usuário logado
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotaSalvaResponse salvar(@Valid @RequestBody SalvarNotaRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return salvar.salvar(usuario.getId(), pedido.getTexto(), pedido.getLink(), pedido.getData());
    }
}