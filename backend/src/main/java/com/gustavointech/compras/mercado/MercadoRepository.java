package com.gustavointech.compras.mercado;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    Optional<Mercado> findByCnpj(String cnpj);
}