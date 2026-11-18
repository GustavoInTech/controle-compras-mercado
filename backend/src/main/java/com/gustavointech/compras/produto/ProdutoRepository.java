package com.gustavointech.compras.produto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Sempre pelo dono: o produto de outro usuário é como se não existisse
    Optional<Produto> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
            select p from Produto p left join fetch p.categoria
            where p.usuario.id = :usuarioId
            order by lower(p.nome)
            """)
    List<Produto> listarDoUsuario(@Param("usuarioId") Long usuarioId);
}