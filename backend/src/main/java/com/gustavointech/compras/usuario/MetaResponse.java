package com.gustavointech.compras.usuario;

import java.math.BigDecimal;

// null quando o usuário não definiu uma meta
public class MetaResponse {

    private final BigDecimal metaMensal;

    public MetaResponse(BigDecimal metaMensal) {
        this.metaMensal = metaMensal;
    }

    public BigDecimal getMetaMensal() {
        return metaMensal;
    }
}