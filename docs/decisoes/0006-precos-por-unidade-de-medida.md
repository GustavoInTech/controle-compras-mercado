# ADR 0006: Preços por unidade de medida

|            |                |
| ---------- | -------------- |
| **Status** | Aceita         |
| **Data**   | (data de hoje) |

## Contexto

Para comparar preços entre mercados e ao longo do tempo (RF13 a RF15), o sistema precisa de um preço por unidade de medida (RN03). Nas notas, parte dos itens vem por peso (unidade KG, preço por kg) e parte por embalagem (UN, PC), com o conteúdo escrito só no nome ("FILE PEITO SEARA 1kg", "HAMB FRIBOI 672G"). O mesmo produto pode ser vendido em embalagens de tamanhos diferentes por mercados diferentes. A nota traz o desconto de forma geral, sem dizer a qual item ele se aplica (RN11).

## Decisão

1. **O conteúdo da embalagem fica no apelido** (usuário, mercado e nome normalizado), e não no produto: o tamanho é propriedade do nome numa loja. Os campos de conteúdo do produto, nunca usados, são removidos.
2. **O sistema sugere o conteúdo lendo o nome original da nota; o usuário confirma.** Nunca é aplicado sozinho, pois a leitura erra com nomes de duas medidas ou de multipacote.
3. **A comparação é feita por família:** g e kg por kg; ml e l por litro; unidade por unidade; sem conteúdo, por embalagem. Itens de outra família que a do item mais recente ficam fora da comparação.
4. **"Ficou mais caro" compara a última compra com a anterior na mesma loja.**
5. **"Onde é mais barato" usa o último preço de cada loja, com a data**, e marca como desatualizado o que tem mais de 90 dias.
6. **O preço usado é o de prateleira impresso na nota**, sem ratear o desconto geral.

## Alternativas consideradas

| Alternativa                                 | Por que não                                                        |
| ------------------------------------------- | ------------------------------------------------------------------ |
| Conteúdo no produto                         | Erra quando duas lojas vendem tamanhos diferentes do mesmo produto |
| Ler o conteúdo do nome na hora, sem guardar | Erra sem o usuário poder corrigir                                  |
| "Ficou mais caro" entre lojas diferentes    | Confunde troca de loja com aumento de preço                        |
| Média das últimas compras                   | Esconde a data e suaviza aumentos recentes                         |

## Consequências

**Positivas:** os preços por kg, litro e unidade ficam comparáveis mesmo com embalagens diferentes; nada é decidido sem o usuário; o aumento de preço é um sinal limpo.

**Negativas e cuidados:**

- A revisão de um nome novo ganha mais um campo (o conteúdo), mitigado pela sugestão com um toque (RNF01).
- Sem conteúdo informado, o item só se compara por embalagem.
- Promoções aplicadas no caixa não aparecem no preço do item.
- Exige a migração V4 (conteúdo no apelido).

**Quando revisar:** se o campo extra pesar na revisão com a família; se o desconto por item passar a vir na nota; se a média de preços for pedida.
