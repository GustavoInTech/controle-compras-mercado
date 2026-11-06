package com.gustavointech.compras.usuario;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "meta_mensal")
    private BigDecimal metaMensal;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    // O JPA precisa de um construtor sem argumentos para criar o objeto ao ler do
    // banco.
    protected Usuario() {
    }

    public Usuario(String email, String senhaHash) {
        // Guardamos o e-mail sempre em minúsculas
        this.email = email.strip().toLowerCase(Locale.ROOT);
        this.senhaHash = senhaHash;
    }

    // Roda automaticamente logo antes do primeiro INSERT
    @PrePersist
    void definirCriadoEm() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public BigDecimal getMetaMensal() {
        return metaMensal;
    }

    public void setMetaMensal(BigDecimal metaMensal) {
        this.metaMensal = metaMensal;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}