package com.gustavointech.compras.nota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// O que o cliente envia para ver a prévia de uma nota
public class PreviaNotaRequest {

    // O texto copiado da página da nota
    @NotBlank
    @Size(max = 100000)
    private String texto;

    // Opcional: o link do QR Code (traz a chave de 44 dígitos quando o texto não
    // traz)
    @Size(max = 2000)
    private String link;

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}