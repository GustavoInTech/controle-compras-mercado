package com.gustavointech.compras.produto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNomeAsc();
}