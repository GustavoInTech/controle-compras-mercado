package com.gustavointech.compras.produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Lista fixa de categorias, criada pela migração V2 (o sistema só lê)
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    protected Categoria() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}