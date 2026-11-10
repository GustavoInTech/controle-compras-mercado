package com.gustavointech.compras.nota;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavointech.compras.usuario.UsuarioDetalhes;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final PreviaNotaService previa;

    public NotaController(PreviaNotaService previa) {
        this.previa = previa;
    }

    // Só lê e confere: não grava nada. Exige login (regra geral do
    // SegurancaConfig).
    @PostMapping("/previa")
    public PreviaNotaResponse previa(@Valid @RequestBody PreviaNotaRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return previa.gerar(usuario.getId(), pedido.getTexto(), pedido.getLink());
    }
}