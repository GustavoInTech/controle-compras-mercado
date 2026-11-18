package com.gustavointech.compras.nota;

import java.math.BigDecimal;

import com.gustavointech.compras.produto.Produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_nota")
public class ItemNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;

    // O produto ao qual este item foi ligado. Vazio = "para revisar" (ADR 0005).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "nome_original", nullable = false, length = 300)
    private String nomeOriginal;

    @Column(name = "nome_normalizado", nullable = false, length = 300)
    private String nomeNormalizado;

    @Column(name = "codigo_mercado", length = 40)
    private String codigoMercado;

    @Column(name = "quantidade", nullable = false)
    private BigDecimal quantidade;

    @Column(name = "unidade", nullable = false, length = 10)
    private String unidade;

    @Column(name = "preco_unitario", nullable = false)
    private BigDecimal precoUnitario;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    protected ItemNota() {
    }

    public ItemNota(String nomeOriginal, String nomeNormalizado, String codigoMercado,
            BigDecimal quantidade, String unidade, BigDecimal precoUnitario, BigDecimal total) {
        this.nomeOriginal = nomeOriginal;
        this.nomeNormalizado = nomeNormalizado;
        this.codigoMercado = codigoMercado;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.total = total;
    }

    // Só a Nota chama isto (por ser do mesmo pacote), ao adicionar o item
    void definirNota(Nota nota) {
        this.nota = nota;
    }

    // Usado ao salvar a nota, para ligar sozinho os nomes que o usuário já
    // confirmou naquela loja
    public void definirProduto(Produto produto) {
        this.produto = produto;
    }

    public Long getId() {
        return id;
    }

    public Nota getNota() {
        return nota;
    }

    public Produto getProduto() {
        return produto;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public String getCodigoMercado() {
        return codigoMercado;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }
}