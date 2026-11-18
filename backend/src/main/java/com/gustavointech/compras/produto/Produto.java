package com.gustavointech.compras.produto;

import java.time.Instant;

import com.gustavointech.compras.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// Produto "de verdade" de um usuário: agrupa itens de nomes diferentes (ADR 0005).
// As colunas conteudo_quantidade e conteudo_unidade serão mapeadas na parte dos preços por kg e litro.
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Pode ser vazia: o produto aparece como "Sem categoria"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Produto() {
    }

    public Produto(Usuario usuario, String nome, Categoria categoria) {
        this.usuario = usuario;
        this.nome = nome;
        this.categoria = categoria;
    }

    @PrePersist
    void definirCriadoEm() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}