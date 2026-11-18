package com.gustavointech.compras.produto;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ProdutoService produtos;

    public CategoriaController(ProdutoService produtos) {
        this.produtos = produtos;
    }

    // A lista fixa de categorias, em ordem alfabética
    @GetMapping
    public List<CategoriaResponse> listar() {
        return produtos.categorias();
    }
}