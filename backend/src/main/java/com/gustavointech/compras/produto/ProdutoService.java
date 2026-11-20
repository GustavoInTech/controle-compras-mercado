package com.gustavointech.compras.produto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.usuario.UsuarioRepository;

@Service
public class ProdutoService {

    // A partir daqui o nome é considerado o MESMO (o pg_trgm devolve 1 para nomes
    // iguais)
    private static final double SIMILARIDADE_MESMO_NOME = 0.999;

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
    // por loja e nome, com sugestões.
    // O sistema sugere, mas nunca aplica sozinho (ADR 0005, decisão 4).
    @Transactional(readOnly = true)
    public List<PendenteResponse> pendentes(Long usuarioId) {
        // As categorias por nome, para transformar a sugestão do dicionário em um id
        Map<String, Categoria> categoriasPorNome = new HashMap<>();
        for (Categoria c : categorias.findAllByOrderByNomeAsc()) {
            categoriasPorNome.put(c.getNome(), c);
        }

        List<PendenteResponse> lista = new ArrayList<>();
        for (Object[] linha : apelidos.listarPendentes(usuarioId)) {
            String nomeNormalizado = (String) linha[2];

            // Colunas 7 a 11: o produto parecido que o usuário já confirmou (vazias quando
            // nada é parecido)
            SugestaoProdutoResponse sugestaoProduto = null;
            SugestaoCategoriaResponse sugestaoCategoria = null;
            if (linha[7] != null) {
                boolean mesmoNome = ((Number) linha[9]).doubleValue() >= SIMILARIDADE_MESMO_NOME;
                sugestaoProduto = new SugestaoProdutoResponse(
                        ((Number) linha[7]).longValue(), (String) linha[8], mesmoNome ? "ALTA" : "MEDIA");
                // O mesmo nome já confirmado em outra loja vale mais que o dicionário
                if (mesmoNome && linha[10] != null) {
                    sugestaoCategoria = new SugestaoCategoriaResponse(
                            ((Number) linha[10]).longValue(), (String) linha[11], "MESMO_NOME");
                }
            }
            if (sugestaoCategoria == null) {
                sugestaoCategoria = categoriaSugerida(nomeNormalizado, categoriasPorNome);
            }

            lista.add(new PendenteResponse(
                    ((Number) linha[0]).longValue(),
                    (String) linha[1],
                    nomeNormalizado,
                    (String) linha[3],
                    (String) linha[4],
                    ((Number) linha[5]).longValue(),
                    LocalDate.parse((String) linha[6]),
                    sugestaoProduto,
                    sugestaoCategoria));
        }
        return lista;
    }

    // Dicionário de palavras; sem pista, a categoria "Outros"
    private static SugestaoCategoriaResponse categoriaSugerida(String nomeNormalizado,
            Map<String, Categoria> categoriasPorNome) {
        Categoria doDicionario = DicionarioCategorias.sugerir(nomeNormalizado)
                .map(categoriasPorNome::get)
                .orElse(null);
        if (doDicionario != null) {
            return new SugestaoCategoriaResponse(doDicionario.getId(), doDicionario.getNome(), "DICIONARIO");
        }
        Categoria outros = categoriasPorNome.get("Outros");
        return outros == null ? null : new SugestaoCategoriaResponse(outros.getId(), outros.getNome(), "PADRAO");
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