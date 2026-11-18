package com.gustavointech.compras.produto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gustavointech.compras.usuario.UsuarioDetalhes;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtos;

    public ProdutoController(ProdutoService produtos) {
        this.produtos = produtos;
    }

    @GetMapping
    public List<ProdutoResponse> listar(@AuthenticationPrincipal UsuarioDetalhes usuario) {
        return produtos.listar(usuario.getId());
    }

    // Os nomes de itens que ainda não foram ligados a um produto
    @GetMapping("/pendentes")
    public List<PendenteResponse> pendentes(@AuthenticationPrincipal UsuarioDetalhes usuario) {
        return produtos.pendentes(usuario.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody CriarProdutoRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return produtos.criar(usuario.getId(), pedido.getNome(), pedido.getCategoriaId(),
                pedido.getMercadoId(), pedido.getNomeNormalizado());
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody ProdutoRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return produtos.atualizar(usuario.getId(), id, pedido.getNome(), pedido.getCategoriaId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioDetalhes usuario) {
        produtos.excluir(usuario.getId(), id);
    }

    // Liga um nome (numa loja) a este produto, em todas as notas. Também corrige
    // uma ligação errada.
    @PostMapping("/{id}/apelidos")
    public AssociacaoResponse associar(@PathVariable("id") Long id, @Valid @RequestBody AssociarRequest pedido,
            @AuthenticationPrincipal UsuarioDetalhes usuario) {
        return produtos.associar(usuario.getId(), id, pedido.getMercadoId(), pedido.getNomeNormalizado());
    }
}