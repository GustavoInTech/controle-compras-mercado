package com.gustavointech.compras.produto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.usuario.UsuarioRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final MercadoRepository mercados;
    private final ProdutoApelidoRepository apelidos;
    private final UsuarioRepository usuarios;

    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias, MercadoRepository mercados,
            ProdutoApelidoRepository apelidos, UsuarioRepository usuarios) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.mercados = mercados;
        this.apelidos = apelidos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> categorias() {
        return categorias.findAllByOrderByNomeAsc().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Long usuarioId) {
        return produtos.listarDoUsuario(usuarioId).stream().map(ProdutoService::resposta).toList();
    }

    // Os nomes que o usuário ainda precisa revisar (itens sem produto), agrupados
    // por loja e nome
    @Transactional(readOnly = true)
    public List<PendenteResponse> pendentes(Long usuarioId) {
        List<PendenteResponse> lista = new ArrayList<>();
        for (Object[] linha : apelidos.listarPendentes(usuarioId)) {
            lista.add(new PendenteResponse(
                    ((Number) linha[0]).longValue(),
                    (String) linha[1],
                    (String) linha[2],
                    (String) linha[3],
                    (String) linha[4],
                    ((Number) linha[5]).longValue(),
                    LocalDate.parse((String) linha[6])));
        }
        return lista;
    }

    // Cria o produto e, se vierem mercadoId e nomeNormalizado, já liga esse nome a
    // ele (e a todas as notas)
    @Transactional
    public ProdutoResponse criar(Long usuarioId, String nome, Long categoriaId,
            Long mercadoId, String nomeNormalizado) {
        boolean comApelido = mercadoId != null || (nomeNormalizado != null && !nomeNormalizado.isBlank());
        if (comApelido && (mercadoId == null || nomeNormalizado == null || nomeNormalizado.isBlank())) {
            throw new PedidoInvalidoException("Para ligar um nome ao produto, informe mercadoId e nomeNormalizado.");
        }

        // Confere o mercado ANTES de criar o produto, para um pedido inválido não
        // deixar nada pela metade
        Mercado mercado = comApelido ? mercadoOuErro(mercadoId) : null;

        Produto produto = produtos.save(
                new Produto(usuarios.getReferenceById(usuarioId), nome.strip(), categoriaOuNull(categoriaId)));
        ProdutoResponse resposta = resposta(produto);
        if (comApelido) {
            associarApelido(usuarioId, produto, mercado, nomeNormalizado);
        }
        return resposta;
    }

    @Transactional
    public ProdutoResponse atualizar(Long usuarioId, Long id, String nome, Long categoriaId) {
        Produto produto = produtoDoUsuario(usuarioId, id);
        produto.setNome(nome.strip());
        produto.setCategoria(categoriaOuNull(categoriaId));
        return resposta(produto);
    }

    // Apaga o produto. O banco faz o resto: os apelidos somem e os itens voltam a
    // ficar "para revisar".
    @Transactional
    public void excluir(Long usuarioId, Long id) {
        produtos.delete(produtoDoUsuario(usuarioId, id));
    }

    // Liga um nome (numa loja) a um produto que já existe. Serve também para
    // corrigir uma ligação errada.
    @Transactional
    public AssociacaoResponse associar(Long usuarioId, Long produtoId, Long mercadoId, String nomeNormalizado) {
        Produto produto = produtoDoUsuario(usuarioId, produtoId);
        Mercado mercado = mercadoOuErro(mercadoId);
        int itens = associarApelido(usuarioId, produto, mercado, nomeNormalizado);
        return new AssociacaoResponse(produtoId, itens);
    }

    // Guarda (ou corrige) o apelido e liga ao produto todos os itens com aquele
    // nome, naquela loja, em todas as notas
    private int associarApelido(Long usuarioId, Produto produto, Mercado mercado, String nomeNormalizado) {
        ProdutoApelido existente = apelidos
                .findByUsuarioIdAndMercadoIdAndNomeNormalizado(usuarioId, mercado.getId(), nomeNormalizado)
                .orElse(null);
        if (existente != null) {
            existente.setProduto(produto);
        } else {
            apelidos.save(new ProdutoApelido(usuarios.getReferenceById(usuarioId), mercado, produto, nomeNormalizado));
        }
        return apelidos.ligarItens(produto.getId(), usuarioId, mercado.getId(), nomeNormalizado);
    }

    private Mercado mercadoOuErro(Long mercadoId) {
        return mercados.findById(mercadoId)
                .orElseThrow(() -> new ReferenciaInvalidaException("Mercado não encontrado."));
    }

    private Produto produtoDoUsuario(Long usuarioId, Long id) {
        return produtos.findByIdAndUsuarioId(id, usuarioId).orElseThrow(ProdutoNaoEncontradoException::new);
    }

    private Categoria categoriaOuNull(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categorias.findById(categoriaId)
                .orElseThrow(() -> new ReferenciaInvalidaException("Categoria não encontrada."));
    }

    private static ProdutoResponse resposta(Produto p) {
        Categoria c = p.getCategoria();
        return new ProdutoResponse(p.getId(), p.getNome(),
                c == null ? null : c.getId(), c == null ? null : c.getNome());
    }
}