package com.gustavointech.compras.nota;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotaRepository extends JpaRepository<Nota, Long> {

        // Esta nota (deste usuário) já foi registrada?
        boolean existsByUsuarioIdAndIdentificador(Long usuarioId, String identificador);

        // Notas de um usuário num período [inicio, fim), já com o mercado carregado
        @Query("""
                        select n from Nota n join fetch n.mercado
                        where n.usuario.id = :usuarioId and n.dataCompra >= :inicio and n.dataCompra < :fim
                        order by n.dataCompra desc, n.id desc
                        """)
        List<Nota> listarDoPeriodo(@Param("usuarioId") Long usuarioId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        // Uma nota com mercado e itens. Sempre filtra pelo usuário: ninguém lê a nota
        // de outro.
        @Query("""
                        select distinct n from Nota n join fetch n.mercado left join fetch n.itens
                        where n.id = :id and n.usuario.id = :usuarioId
                        """)
        Optional<Nota> buscarComItens(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

        // Quantos itens tem cada nota, numa consulta só para a lista inteira: cada
        // linha é [id da nota, quantidade]
        @Query("""
                        select i.nota.id, count(i) from ItemNota i
                        where i.nota.id in :ids group by i.nota.id
                        """)
        List<Object[]> contarItens(@Param("ids") Collection<Long> ids);

        // Os totais de um período [inicio, fim). Devolve UMA linha: [soma do valor a
        // pagar, soma dos descontos, quantas notas].
        // Mês sem notas dá 0, 0, 0 (COALESCE troca o "vazio" da soma por zero).
        @Query(value = """
                        SELECT COALESCE(SUM(valor_a_pagar), 0), COALESCE(SUM(desconto), 0), COUNT(*)
                        FROM nota
                        WHERE usuario_id = :usuarioId AND data_compra >= :inicio AND data_compra < :fim
                        """, nativeQuery = true)
        List<Object[]> totaisDoPeriodo(@Param("usuarioId") Long usuarioId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        // O valor BRUTO dos itens (antes do desconto) por categoria, da maior para a
        // menor. Cada linha é:
        // [id da categoria, nome da categoria, soma]. Item sem produto, ou de produto
        // sem categoria, vem com id e nome vazios.
        @Query(value = """
                        SELECT c.id, c.nome, SUM(i.total)
                        FROM item_nota i
                        JOIN nota n ON n.id = i.nota_id
                        LEFT JOIN produto p ON p.id = i.produto_id
                        LEFT JOIN categoria c ON c.id = p.categoria_id
                        WHERE n.usuario_id = :usuarioId AND n.data_compra >= :inicio AND n.data_compra < :fim
                        GROUP BY c.id, c.nome
                        ORDER BY SUM(i.total) DESC, c.nome
                        """, nativeQuery = true)
        List<Object[]> gastoPorCategoria(@Param("usuarioId") Long usuarioId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);
}