package com.gustavointech.compras.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Compara em minúsculas dos dois lados: bate com o índice único do banco
    // (lower(email))
    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    // Apaga o usuário. O banco apaga em cascata as notas, itens, produtos e
    // apelidos dele (ON DELETE CASCADE).
    @Modifying
    @Query("delete from Usuario u where u.id = :id")
    void excluirPorId(@Param("id") Long id);
}