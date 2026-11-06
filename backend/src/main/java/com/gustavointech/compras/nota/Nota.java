package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.usuario.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "nota")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercado_id", nullable = false)
    private Mercado mercado;

    // Evita nota duplicada: a chave de 44 dígitos, ou cnpj|serie|numero, ou uma
    // impressão digital
    @Column(name = "identificador", nullable = false, length = 64)
    private String identificador;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "desconto", nullable = false)
    private BigDecimal desconto;

    @Column(name = "valor_a_pagar", nullable = false)
    private BigDecimal valorAPagar;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemNota> itens = new ArrayList<>();

    protected Nota() {
    }

    public Nota(Usuario usuario, Mercado mercado, String identificador, String chaveAcesso,
            LocalDate dataCompra, BigDecimal valorTotal, BigDecimal desconto, BigDecimal valorAPagar) {
        this.usuario = usuario;
        this.mercado = mercado;
        this.identificador = identificador;
        this.chaveAcesso = chaveAcesso;
        this.dataCompra = dataCompra;
        this.valorTotal = valorTotal;
        this.desconto = desconto;
        this.valorAPagar = valorAPagar;
    }

    @PrePersist
    void definirCriadoEm() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    // Mantém os dois lados do relacionamento em sincronia
    public void adicionarItem(ItemNota item) {
        item.definirNota(this);
        itens.add(item);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getValorAPagar() {
        return valorAPagar;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    // Visão que não pode ser alterada de fora: só a própria nota mexe nos itens
    public List<ItemNota> getItens() {
        return Collections.unmodifiableList(itens);
    }
}