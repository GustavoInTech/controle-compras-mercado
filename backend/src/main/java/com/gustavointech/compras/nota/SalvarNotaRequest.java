package com.gustavointech.compras.nota;

import java.time.LocalDate;

// O que o cliente envia para salvar uma nota: o mesmo da prévia, mais a data (quando o texto não traz)
public class SalvarNotaRequest extends PreviaNotaRequest {

    // Opcional: só vale se a data não vier no texto da nota
    private LocalDate data;

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}