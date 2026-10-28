# Documento de Requisitos: Controle de Compras de Mercado

| | |
|---|---|
| **Versão** | 0.3 (rascunho) |
| **Data** | 27/10/2026 |
| **Autor** | Gustavo |
| **Status do projeto** | Em fase de requisitos. Ainda não há código do sistema; existe um protótipo do leitor de notas (ver seção 8). |

---

## 1. Contexto e problema

Quem faz compras de mercado dificilmente sabe quanto gastou no mês, quais produtos ficaram mais caros ou em qual mercado compensa comprar cada item. Na pesquisa feita para este projeto, a resposta sobre controle atual foi "não controlo". A concorrência real, portanto, é **não fazer nada**, e o motivo é o trabalho de anotar cada produto e preço à mão.

**Ideia central:** o sistema deve dar essa visão ao usuário **sem exigir digitação de produtos**, aproveitando a nota fiscal eletrônica (NFC-e) que o próprio mercado já emite.

## 2. Usuários

| Perfil | Descrição | Uso esperado |
|---|---|---|
| Usuário comum (família e amigos) | Faz compras em cerca de 6 mercados diferentes, guarda as notas, usa celular ou computador | Registrar notas e consultar gastos e preços |
| Administrador (o autor) | Mantém o sistema | Acompanhar erros de leitura de notas |

Fase atual: uso privado (autor, família e alguns amigos). Abertura ao público só depois de a versão 1 estar estável.

## 3. Pesquisa com usuários

Entrevistas informais com 15 pessoas (família e amigos).

| Pergunta | Resposta | O que isso significa para o sistema |
|---|---|---|
| Como controla os gastos hoje? | Não controla | O sistema compete com "nada"; precisa ser mais fácil que não fazer |
| O que gostaria de saber? | Gasto no mês; produtos que ficaram mais caros; onde cada produto é mais barato; gasto por categoria | Define as quatro funções centrais (RF09 a RF15) |
| Guarda notas? Pede CPF? | Guardam as notas e costumam pedir CPF | O CPF aparece no texto da nota; o sistema deve ignorá-lo e nunca armazená-lo (RNF04) |
| Celular ou computador? | Dividido | O sistema deve funcionar bem nos dois (RNF02) |
| O que faria desistir? | Preencher muita informação; demorar para registrar; dificuldade de uso | Registrar uma compra deve ser rápido e quase sem digitação (RNF01) |
| Quantos mercados? | Em média 6 | Comparar preços entre mercados é central, e o mesmo produto terá nomes diferentes em cada um (RF12) |

**Tensão identificada:** os usuários querem gasto por categoria, mas desistem se tiverem que preencher muita coisa. A nota fiscal não traz categoria. Solução proposta: o sistema **sugere** a categoria, o usuário confirma uma única vez por produto e o sistema lembra (RF11).

## 4. Escopo da versão 1

**Dentro:**
- Cadastro e login
- Entrada de notas por texto copiado da página da NFC-e (SEFAZ-RJ)
- Histórico de notas e itens
- Gasto mensal total e por categoria
- Meta mensal de gasto (valor digitado pelo usuário, com indicador de progresso)
- Evolução de preço por produto e comparação entre mercados

**Fora (decisão consciente, ver seção 9):**
- Inteligência artificial
- Comparador colaborativo entre usuários
- Aplicativo de loja (Android/iOS)
- Cobrança e planos
- Outros estados (o leitor é feito para o layout da SEFAZ-RJ)
- Notas de papel sem QR Code

## 5. Requisitos funcionais

Prioridade (MoSCoW): **M** = essencial na v1, **S** = importante, **C** = desejável.

| ID | Requisito | Prior. |
|---|---|---|
| RF01 | O usuário pode se cadastrar com e-mail e senha | M |
| RF02 | O usuário pode entrar e sair do sistema | M |
| RF03 | O usuário cola o texto copiado da nota e vê uma **prévia** (mercado, data, quantidade de itens, total) antes de salvar | M |
| RF04 | O sistema valida a leitura (contagem de itens, soma dos valores, CNPJ) e avisa claramente quando algo não confere, sem salvar dados parciais | M |
| RF05 | O sistema impede o cadastro da mesma nota duas vezes | M |
| RF06 | O sistema orienta o usuário a expandir "Informações gerais da Nota" antes de copiar (é ali que vêm a data e o protocolo) e, se a data ainda faltar, pede que ele informe | M |
| RF07 | O sistema salva a nota e seus itens vinculados ao usuário | M |
| RF08 | O usuário vê o histórico de notas e os itens de cada uma | M |
| RF09 | O usuário vê o gasto total de um mês | M |
| RF10 | O usuário vê o gasto do mês por categoria | M |
| RF11 | O sistema sugere a categoria de cada produto, o usuário confirma uma vez e o sistema lembra | M |
| RF12 | O sistema agrupa itens que são o mesmo produto com nomes diferentes (entre mercados), pedindo confirmação ao usuário na primeira vez e lembrando depois | M |
| RF13 | O usuário vê a evolução do preço de um produto ao longo do tempo (por kg, litro ou unidade) | S |
| RF14 | O usuário compara o preço do mesmo produto entre mercados | S |
| RF15 | O usuário vê quais produtos ficaram mais caros desde a última compra | S |
| RF16 | O usuário compara o gasto de um mês com o anterior | S |
| RF17 | O usuário pode excluir uma nota e corrigir categoria ou associação de produto | S |
| RF18 | O usuário pode excluir a própria conta e todos os seus dados | S |
| RF19 | O usuário define uma meta mensal de gasto (um valor opcional) e vê quanto dela já foi usado no mês. Avisos por notificação ficam fora da v1 | S |
| RF20 | O usuário exporta seus dados (CSV) | C |
| RF21 | Leitura do QR Code pela câmera dentro do sistema | C |
| RF22 | Entrada de nota por print da tela (OCR) | C |

## 6. Requisitos não funcionais

| ID | Requisito | Meta proposta (a confirmar) |
|---|---|---|
| RNF01 | **Usabilidade:** registrar uma compra deve ser rápido e quase sem digitação | Depois da nota aberta: colar, conferir e salvar em até 1 minuto |
| RNF02 | **Responsivo:** funciona em celular e computador | Projetado primeiro para celular |
| RNF03 | **Precisão monetária:** nenhum erro de arredondamento | Valores em `NUMERIC` no banco e `BigDecimal` no Java |
| RNF04 | **Privacidade (LGPD):** o texto bruto da nota e o CPF nunca são armazenados; cada usuário vê somente os próprios dados; aviso claro de quais dados são guardados | Verificado por teste automatizado |
| RNF05 | **Segurança:** senha com hash (BCrypt), HTTPS, segredos fora do repositório | |
| RNF06 | **Desempenho:** prévia da nota rápida | Até 3 segundos para notas de 100 itens |
| RNF07 | **Custo:** operar com custo zero ou mínimo na fase inicial | |
| RNF08 | **Manutenibilidade:** o leitor de notas é um módulo isolado e testado com notas reais anonimizadas, para que mudanças no layout da SEFAZ afetem só ele | |
| RNF09 | **Confiabilidade:** se o leitor não entender o texto, mostra erro claro em vez de salvar dados errados | |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | O valor de cada item é o "Vl. Total" impresso na nota, nunca recalculado (a nota trunca os centavos: 0,245 kg × R$ 3,99 = 0,97755, e a nota cobra 0,97) |
| RN02 | A soma dos itens deve ser igual ao "Valor total" (quando existir) ou ao "Valor a pagar"; e "Valor total − Descontos = Valor a pagar" |
| RN03 | Comparações de preço usam sempre o preço por unidade de medida (kg, litro, unidade), nunca o total pago |
| RN04 | Itens repetidos em linhas separadas da mesma nota, com o mesmo código, são o mesmo produto e devem ser agrupados |
| RN05 | O "Código" do item é interno de cada mercado e **não** identifica o produto entre mercados. Se vier um código de barras (EAN), ele pode ajudar na associação |
| RN06 | Cada nota é única, por usuário, pela chave de acesso de 44 dígitos; se a chave não vier, pelo trio CNPJ + série + número (que o texto traz em "Informações gerais"); se também faltar, por uma impressão digital (CNPJ, data, total e itens) |
| RN07 | O mercado é identificado pelo CNPJ completo, ou seja, cada **loja** é um mercado, e as comparações de preço são feitas por loja (decisão de 27/10/2026). Filiais da mesma rede compartilham os 8 primeiros dígitos, então agrupar por rede continua possível no futuro sem mudar o banco |
| RN08 | A associação entre produtos de nomes diferentes exige confirmação do usuário na primeira vez; depois é lembrada |
| RN09 | A data da compra é a data de emissão da nota; se não vier, o usuário informa |
| RN10 | Itens como sacola contam no gasto total, mas ficam fora das comparações de preço (a definir) |
| RN11 | O gasto do mês é a soma do "Valor a pagar" das notas (o que realmente saiu do bolso). O gasto por categoria soma os itens pelo valor bruto, porque a nota não diz a qual item o desconto se aplica; a diferença aparece como "Descontos" |

## 8. O que já foi validado (prova de conceito)

Antes de projetar, foram feitos testes para reduzir risco. Detalhes na pasta `prototipos/`.

- **Leitura automática pelo servidor: descartada.** O servidor da Fazenda do RJ bloqueia acessos automatizados (devolve página de bloqueio de IP, embora a nota abra no navegador). Decidiu-se **não contornar** essa barreira (segurança, LGPD e risco jurídico).
- **Entrada por texto copiado: viável.** O leitor extraiu corretamente duas notas reais: uma de 10 itens (açougue) e uma de 63 itens com desconto (atacadista), conferindo a soma dos itens.
- **Achados:** a versão do celular, com a seção "Informações gerais da Nota" recolhida, não traz data nem chave de acesso; a nota trunca os centavos; itens repetidos vêm em linhas separadas; o código do item é interno do mercado; o peso/volume está dentro do nome do produto.
- **Teste no celular (27/10/2026):** ao expandir "Informações gerais da Nota" antes de copiar, o texto passa a trazer a data de emissão e o protocolo. Falta confirmar se a chave de acesso também vem.

## 9. Riscos

| ID | Risco | Mitigação |
|---|---|---|
| R1 | A SEFAZ muda o layout da página e o leitor quebra | Módulo isolado, testes com notas reais e avisos de conferência |
| R2 | Copiar e colar cansa o usuário, e a pesquisa mostra que trabalho demais faz desistir | Medir o uso real; plano B: print + OCR, depois navegador embutido e IA |
| R3 | O mesmo produto tem nomes muito diferentes nos 6 mercados | Confirmação do usuário na primeira vez; avaliar IA depois |
| R4 | Dados pessoais de compra e CPF no texto | Não armazenar texto bruto nem CPF; isolamento por usuário; exclusão de conta |
| R5 | Outros mercados trazem formatos que o leitor ainda não viu (acréscimos, vários pagamentos, código de barras) | Coletar notas anonimizadas dos 6 mercados dos entrevistados e testar |
| R6 | Hospedagem gratuita de Java é limitada | Avaliar opções antes do deploy |

## 10. Decisões e pontos em aberto

| # | Pergunta | Situação |
|---|---|---|
| 1 | Expandir "Informações gerais da Nota" no celular traz a chave e a data? | **A data vem** (testado em 27/10/2026). A chave ainda precisa ser confirmada; se não vier, vale a impressão digital da nota (RN06) |
| 2 | Os entrevistados pedem CPF na nota? | **Sim.** O CPF aparece no texto copiado; o sistema o ignora e avisa o usuário (RNF04) |
| 3 | Comparar por rede ou por loja? | **Por loja** (RN07) |
| 4 | Lista de categorias padrão | **Aprovada:** Hortifruti, Carnes, Laticínios, Bebidas, Padaria, Mercearia, Congelados, Limpeza, Higiene, Outros |
| 5 | A meta mensal (RF19) entra na v1? | **Sim, em versão simples:** o próprio usuário digita o valor e o sistema mostra o progresso. Notificações ficam para depois |

## 11. Critérios de sucesso da v1 (propostos)

- O autor e pelo menos 3 pessoas da família usam o sistema por 4 semanas seguidas
- Registrar uma nota leva menos de 1 minuto
- Notas dos 6 mercados testados são lidas corretamente (soma confere)
- Nenhum dado pessoal (CPF, texto bruto) é armazenado

## 12. Histórico do documento

| Versão | Data | Mudança |
|---|---|---|
| 0.1 | 27/10/2026 | Primeira versão, a partir da pesquisa com usuários e da prova de conceito |
| 0.2 | 27/10/2026 | Respostas das perguntas abertas, número de entrevistados e teste no celular |
| 0.3 | 27/10/2026 | Meta mensal entra na v1 (versão simples); RN06 detalhada; nova RN11 (gasto e descontos) |
