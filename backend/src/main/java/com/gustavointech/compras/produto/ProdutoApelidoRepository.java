package com.gustavointech.compras.produto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoApelidoRepository extends JpaRepository<ProdutoApelido, Long> {

    Optional<ProdutoApelido> findByUsuarioIdAndMercadoIdAndNomeNormalizado(
            Long usuarioId, Long mercadoId, String nomeNormalizado);

    List<ProdutoApelido> findByUsuarioIdAndMercadoId(Long usuarioId, Long mercadoId);

    // Liga ao produto TODOS os itens do usuário, naquele mercado, com aquele nome:
    // de qualquer nota, passada ou
    // futura (ADR 0005, decisão 3). Se já estavam ligados a outro produto, são
    // religados. Devolve quantos itens mudaram.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE item_nota SET produto_id = :produtoId
            WHERE nome_normalizado = :nome
              AND nota_id IN (SELECT id FROM nota WHERE usuario_id = :usuarioId AND mercado_id = :mercadoId)
            """, nativeQuery = true)
    int ligarItens(@Param("produtoId") Long produtoId,
            @Param("usuarioId") Long usuarioId,
            @Param("mercadoId") Long mercadoId,
            @Param("nome") String nome);

    // Os nomes ainda sem produto, agrupados por mercado e nome. Cada linha é:
    // [id do mercado, nome do mercado, nome normalizado, um nome original de
    // exemplo, unidade, quantas linhas, última compra (AAAA-MM-DD)]
    @Query(value = """
            SELECT n.mercado_id, m.nome, i.nome_normalizado, MIN(i.nome_original), MIN(i.unidade),
                   COUNT(*), TO_CHAR(MAX(n.data_compra), 'YYYY-MM-DD')
            FROM item_nota i
            JOIN nota n ON n.id = i.nota_id
            JOIN mercado m ON m.id = n.mercado_id
            WHERE n.usuario_id = :usuarioId AND i.produto_id IS NULL
            GROUP BY n.mercado_id, m.nome, i.nome_normalizado
            ORDER BY MAX(n.data_compra) DESC, m.nome, i.nome_normalizado
            """, nativeQuery = true)
    List<Object[]> listarPendentes(@Param("usuarioId") Long usuarioId);
}