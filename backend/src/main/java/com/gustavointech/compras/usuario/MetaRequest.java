package com.gustavointech.compras.usuario;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

// O que o cliente envia para definir a meta mensal
public class MetaRequest {

    // Entre R$ 0,01 e R$ 10.000.000,00, com no máximo 2 casas decimais (cabe na
    // coluna NUMERIC(12,2))
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("10000000")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal valor;

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}