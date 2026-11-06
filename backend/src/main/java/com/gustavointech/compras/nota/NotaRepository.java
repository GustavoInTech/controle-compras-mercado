package com.gustavointech.compras.nota;

import java.time.LocalDate;
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
}