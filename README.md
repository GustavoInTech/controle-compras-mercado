# Controle de Compras de Mercado

Sistema web para registrar compras de supermercado a partir da nota fiscal eletrônica (NFC-e) e acompanhar gastos e preços ao longo do tempo.

> **Status:** em desenvolvimento. Fase atual: levantamento de requisitos e planejamento. Ainda não há versão utilizável.

## Problema

Quem compra em vários mercados raramente sabe quanto gastou no mês, quais produtos ficaram mais caros ou onde cada item compensa mais. Anotar produto por produto dá trabalho demais. Este projeto busca dar essa visão com o mínimo de digitação.

## O que o sistema vai fazer (planejado)

- Cadastro e login
- Registrar uma compra colando o texto da nota fiscal (NFC-e do Rio de Janeiro)
- Gasto do mês, total e por categoria
- Evolução do preço de cada produto
- Comparação de preços entre mercados e entre meses

## Stack planejada

Java e Spring Boot (backend), PostgreSQL (banco de dados), React com TypeScript (frontend).

## Documentação

- [Documento de requisitos](docs/01-requisitos.md)

## Privacidade

Princípio do projeto: não armazenar o texto bruto da nota fiscal nem o CPF do consumidor.

## Autor

Gustavo ([@GustavoInTech](https://github.com/GustavoInTech))
