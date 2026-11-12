package com.gustavointech.compras.nota;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gustavointech.compras.usuario.UsuarioDetalhes;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final PreviaNotaService previa;
    private final SalvarNotaService salvar;
    private final ConsultaNotaService consulta;

    public NotaController(PreviaNotaService previa, SalvarNotaService salvar, ConsultaNotaService consulta) {
        this.previa = previa;
        this.salvar = salvar;
        this.consulta = consulta;
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

    // As notas de um mês (padrão: o mês atual), da mais nova para a mais antiga
    @GetMapping
    public List<NotaResumoResponse> listar(@RequestParam(name = "ano", required = false) Integer ano,
            @RequestParam(name = "mes", required = false) Integer mes,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return consulta.listar(usuario.getId(), mesPedido(ano, mes));
    }

    @GetMapping("/{id}")
    public NotaDetalheResponse detalhar(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return consulta.detalhar(usuario.getId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioDetalhes usuario) {
        consulta.excluir(usuario.getId(), id);
    }

    // Sem parâmetros: o mês atual. Com ano e mês: aquele mês. Só um dos dois, ou
    // valor impossível: 400.
    private static YearMonth mesPedido(Integer ano, Integer mes) {
        if (ano == null && mes == null) {
            return YearMonth.now();
        }
        if (ano == null || mes == null || ano < 2000 || ano > 2100 || mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ano (2000 a 2100) e mês (1 a 12).");
        }
        return YearMonth.of(ano, mes);
    }
}