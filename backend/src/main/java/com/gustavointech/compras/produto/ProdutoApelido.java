package com.gustavointech.compras.produto;

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// A memória do sistema: "nesta loja, este nome normalizado é este produto" (a unidade de decisão do ADR 0005)
@Entity
@Table(name = "produto_apelido")
public class ProdutoApelido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercado_id", nullable = false)
    private Mercado mercado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "nome_normalizado", nullable = false, length = 300)
    private String nomeNormalizado;

    protected ProdutoApelido() {
    }

    public ProdutoApelido(Usuario usuario, Mercado mercado, Produto produto, String nomeNormalizado) {
        this.usuario = usuario;
        this.mercado = mercado;
        this.produto = produto;
        this.nomeNormalizado = nomeNormalizado;
    }

    public Long getId() {
        return id;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public Produto getProduto() {
        return produto;
    }

    // Corrigir uma associação é apontar o mesmo apelido para outro produto
    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }
}