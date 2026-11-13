# ADR 0005: Produtos, categorias e associação de nomes

|            |            |
| ---------- | ---------- |
| **Status** | Aceita     |
| **Data**   | 10/11/2026 |

## Contexto

Cada mercado escreve o nome do mesmo produto de um jeito, e o código do item é interno de cada loja (RN05). A pesquisa mostrou cerca de 6 mercados por pessoa e que preencher muita informação faz o usuário desistir (RNF01). A nota não traz categoria, e a versão 1 não usa IA.

## Decisão

1. **Um produto só nasce quando o usuário o confirma**, criando-o ou ligando o nome a um produto existente. Ao salvar uma nota, o sistema só liga automaticamente os itens cujo nome já tem apelido conhecido naquela loja. O resto fica "para revisar", e a nota é salva do mesmo jeito.
2. **A unidade de decisão é o apelido:** usuário, mercado e nome normalizado. Uma confirmação vale para todas as linhas daquele nome, em qualquer nota.
3. **A ligação é retroativa.** Ao confirmar ou corrigir um apelido, todos os itens do usuário com aquele mercado e nome são ligados (ou religados) ao produto, para o histórico de preços ficar inteiro.
4. **O sistema sugere, nunca aplica sozinho.** A categoria é sugerida pelo mesmo nome já confirmado em outro mercado, por um dicionário de palavras no código (testado com notas reais) ou, sem pista, "Outros". O produto parecido é sugerido por similaridade de texto (`pg_trgm`).
5. **As categorias são uma lista fixa** na versão 1. Produtos e apelidos pertencem a cada usuário, sem catálogo compartilhado.

## Alternativas consideradas

| Alternativa                                      | Por que não                                                             |
| ------------------------------------------------ | ----------------------------------------------------------------------- |
| Criar um produto para cada nome ao salvar a nota | Enche o catálogo de duplicatas que o usuário teria de juntar depois     |
| Aplicar as sugestões automaticamente             | Um erro silencioso estraga as comparações de preço sem ninguém perceber |
| Valer só para itens futuros                      | Quebra o histórico de preços, que é o principal valor do produto        |
| Usar IA para sugerir                             | Adiada: custo e dados pessoais na versão 1                              |

## Consequências

**Positivas:** salvar a nota não pede nada ao usuário; o histórico de cada produto fica completo; nada é decidido sem o usuário.

**Negativas e cuidados:**

- O usuário revisa cada nome novo uma vez por mercado (risco R2 e RNF01). Mitigações: a revisão não bloqueia nada, as sugestões servem com um toque e as de alta confiança podem ser confirmadas em lote (a desenhar com a tela).
- O dicionário de palavras erra com abreviações como `LTE INT` (leite integral). O usuário corrige (RF17).
- Exige a extensão `pg_trgm` do PostgreSQL (migração V3).

**Quando revisar:** se a revisão se mostrar cansativa nos testes com a família, se o uso de IA for adotado, ou se um catálogo compartilhado entre usuários for pensado (com cuidado de privacidade).
