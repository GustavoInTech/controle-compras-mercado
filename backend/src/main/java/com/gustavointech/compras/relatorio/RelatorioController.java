package com.gustavointech.compras.relatorio;

import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gustavointech.compras.usuario.UsuarioDetalhes;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorios;

    public RelatorioController(RelatorioService relatorios) {
        this.relatorios = relatorios;
    }

    // O dashboard de um mês (padrão: o mês atual)
    @GetMapping("/mes")
    public RelatorioMesResponse mes(@RequestParam(name = "ano", required = false) Integer ano,
            @RequestParam(name = "mes", required = false) Integer mes,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return relatorios.mes(usuario.getId(), mesPedido(ano, mes));
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