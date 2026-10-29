# ADR 0003: Java, Spring Boot, PostgreSQL e React

| | |
|---|---|
| **Status** | Aceita |
| **Data** | 27/10/2026 |

## Contexto

O sistema precisa de login, banco de dados relacional, leitura e validação de texto, consultas agregadas (gasto por mês, por categoria, evolução de preço) e uma interface que funcione no celular e no computador. O autor trabalha com Java no dia a dia e o projeto tem também objetivo de aprendizado e portfólio. Será usado, de início, por família e amigos.

## Decisão

| Camada | Escolha |
|---|---|
| Backend | Java 25 (versão LTS) com Spring Boot 4.1 e Maven. Confirmar a versão mais recente em start.spring.io ao criar o projeto |
| Banco de dados | PostgreSQL, com migrações versionadas (Flyway) |
| Frontend | React com TypeScript (Vite) |
| Estilo de arquitetura | Monólito, API REST em JSON |

## Por quê

- **Java é a linguagem em que o autor tem experiência**, o que reduz o tempo de construção e deixa o foco em engenharia (requisitos, modelagem, testes) e não em aprender uma linguagem nova.
- **Os dados são relacionais** (usuário, nota, item, produto, mercado) e as telas dependem de somas e comparações. PostgreSQL resolve isso bem e tem busca por similaridade de texto, útil para o reconhecimento de produtos (RF12).
- **Dinheiro exige precisão:** Java tem `BigDecimal` e o PostgreSQL tem `NUMERIC` (RNF03).
- **Spring Boot 4.x** é a linha atual do framework. A linha 3.5, segundo as fontes consultadas em 27/10/2026, teve o suporte gratuito encerrado em 30/06/2026 (a confirmar na página oficial do Spring). Começar na 3.5 significaria nascer desatualizado.
- **React com TypeScript** tem amplo uso no mercado, e o frontend separado do backend deixa as duas partes evoluírem independentemente.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Next.js + Supabase (JavaScript/TypeScript em tudo) | Seria mais rápido de começar e de hospedar de graça, e traz login e controle de acesso prontos. Mas não aproveita a experiência do autor com Java |
| Python no backend | Ótimo para tratar texto, mas adicionaria uma terceira linguagem, sem vantagem decisiva para este projeto |
| Microsserviços | Complexidade desnecessária para um projeto de uma pessoa e poucos usuários |

## Consequências

**Positivas:** uso da experiência existente; stack comum no mercado; base sólida para testes automatizados.

**Negativas e cuidados:**
- Hospedagem gratuita de Java é mais limitada do que a de Node (risco R6), e os planos atuais precisam ser conferidos antes do deploy.
- São duas linguagens (Java e TypeScript).
- O isolamento dos dados por usuário não vem pronto no banco: é responsabilidade do código e deve ter testes automatizados (ver arquitetura, seção 7).
- O Spring Boot publica versões novas com frequência. É preciso reservar tempo para atualizações ao longo do projeto.
