package com.gustavointech.compras.leitor;

import java.util.Objects;

public final class MercadoLido {

    private final String nome;
    private final String cnpj;
    private final String endereco;

    public MercadoLido(String nome, String cnpj, String endereco) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEndereco() {
        return endereco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MercadoLido that))
            return false;
        return Objects.equals(nome, that.nome)
                && Objects.equals(cnpj, that.cnpj)
                && Objects.equals(endereco, that.endereco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cnpj, endereco);
    }

    @Override
    public String toString() {
        return "MercadoLido{nome=" + nome + ", cnpj=" + cnpj + ", endereco=" + endereco + "}";
    }
}