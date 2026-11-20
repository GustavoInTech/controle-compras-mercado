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

        // Os nomes ainda sem produto, agrupados por mercado e nome, cada um com o
        // produto mais parecido entre os que o
        // usuário já confirmou (nomes de apelidos). Cada linha é:
        // 0 id do mercado, 1 nome do mercado, 2 nome normalizado, 3 um nome original de
        // exemplo, 4 unidade,
        // 5 quantas linhas, 6 última compra (AAAA-MM-DD),
        // 7 id do produto parecido, 8 nome dele, 9 similaridade (1 = mesmo nome), 10 id
        // da categoria dele, 11 nome dela
        // As colunas 7 a 11 vêm vazias quando nada é parecido. O operador % (extensão
        // pg_trgm) usa o índice e filtra
        // pelo limite padrão de 0,3; a segunda condição aperta para 0,4, porque 0,3
        // aceitava pares como
        // CEBOLA KG e CENOURA KG (0,31), que são produtos diferentes.
        @Query(value = """
                        SELECT p.mercado_id, p.mercado_nome, p.nome_normalizado, p.nome_original, p.unidade, p.ocorrencias,
                               p.ultima_compra, s.produto_id, s.produto_nome, s.similaridade, s.categoria_id, s.categoria_nome
                        FROM (
                            SELECT n.mercado_id AS mercado_id, m.nome AS mercado_nome, i.nome_normalizado AS nome_normalizado,
                                   MIN(i.nome_original) AS nome_original, MIN(i.unidade) AS unidade, COUNT(*) AS ocorrencias,
                                   TO_CHAR(MAX(n.data_compra), 'YYYY-MM-DD') AS ultima_compra, MAX(n.data_compra) AS data_ordem
                            FROM item_nota i
                            JOIN nota n ON n.id = i.nota_id
                            JOIN mercado m ON m.id = n.mercado_id
                            WHERE n.usuario_id = :usuarioId AND i.produto_id IS NULL
                            GROUP BY n.mercado_id, m.nome, i.nome_normalizado
                        ) p
                        LEFT JOIN LATERAL (
                            SELECT pr.id AS produto_id, pr.nome AS produto_nome,
                                   similarity(pa.nome_normalizado, p.nome_normalizado) AS similaridade,
                                   pr.categoria_id AS categoria_id, c.nome AS categoria_nome
                            FROM produto_apelido pa
                            JOIN produto pr ON pr.id = pa.produto_id
                            LEFT JOIN categoria c ON c.id = pr.categoria_id
                            WHERE pa.usuario_id = :usuarioId
                              AND pa.nome_normalizado % p.nome_normalizado
                              AND similarity(pa.nome_normalizado, p.nome_normalizado) >= 0.4
                            ORDER BY similarity(pa.nome_normalizado, p.nome_normalizado) DESC, pr.id
                            LIMIT 1
                        ) s ON TRUE
                        ORDER BY p.data_ordem DESC, p.mercado_nome, p.nome_normalizado
                        """, nativeQuery = true)
        List<Object[]> listarPendentes(@Param("usuarioId") Long usuarioId);
}