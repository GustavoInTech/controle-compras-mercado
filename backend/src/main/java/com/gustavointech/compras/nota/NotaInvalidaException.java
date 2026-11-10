package com.gustavointech.compras.nota;

import java.util.List;

// A nota não pôde ser salva: o texto não confere, falta um dado ou algum valor é estranho
public class NotaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> motivos;

    public NotaInvalidaException(List<String> motivos) {
        super("A nota não pôde ser salva.");
        this.motivos = List.copyOf(motivos);
    }

    public List<String> getMotivos() {
        return motivos;
    }
}