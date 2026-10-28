# ADR 0002: Entrada de notas por texto copiado

| | |
|---|---|
| **Status** | Aceita (para a versão 1) |
| **Data** | 27/10/2026 |

## Contexto

Com a leitura automática descartada ([ADR 0001](0001-nao-ler-site-da-fazenda.md)), é preciso outra forma de os dados da nota chegarem ao sistema. A pesquisa com 15 pessoas mostrou que o que faz alguém desistir de um app desses é preencher muita informação e demorar para registrar uma compra (RNF01). Digitar produto por produto está, portanto, fora de cogitação.

Foi testado o caminho de copiar o texto da página da nota. Um protótipo de leitor extraiu corretamente duas notas reais: uma de 10 itens (açougue) e uma de 63 itens com desconto (atacadista), conferindo que a soma dos itens bate com o valor da nota.

## Decisão

Na versão 1, o usuário **escaneia o QR Code com a câmera do celular, abre a nota no navegador, copia o texto e cola no sistema**. O backend lê o texto (módulo `leitor`), valida e mostra uma prévia antes de salvar.

## Alternativas consideradas

| Alternativa | Situação |
|---|---|
| Digitar produtos manualmente | Descartada: contradiz o principal motivo de desistência |
| Print da nota aberta + OCR gratuito (por exemplo, Tesseract.js) | Adiada. Candidata a segunda forma de entrada, por exigir só um print. O texto na tela é limpo, o que favorece o OCR |
| Navegador embutido em app de celular, leitura em um toque | Adiada. Melhor experiência, mas exige app de celular e depende de testar se o site da Fazenda aceita esse acesso |
| Foto da nota de papel lida por IA | Adiada. Fica como reserva para notas sem QR Code e para depois da v1 |

## Consequências

**Positivas**
- Custo zero e sem IA na v1.
- O leitor é código comum, testável com notas reais.
- Funciona no celular e no computador.

**Negativas e cuidados**
- **Há atrito**: são vários passos. Se os usuários desistirem por isso (risco R2), o plano é adicionar print + OCR e depois o navegador embutido.
- **O texto copiado pode conter o CPF** do consumidor. O sistema o ignora, não o grava e avisa o usuário na tela de colar (RNF04).
- **A versão do celular só traz a data e o protocolo se a seção "Informações gerais da Nota" estiver aberta** antes de copiar. O sistema orienta o usuário sobre isso (RF06).
- O leitor depende do formato do texto da SEFAZ-RJ. Por isso fica isolado em módulo próprio, com testes e validações (soma dos itens, contagem, CNPJ). Se o formato mudar, só ele muda (RNF08).
- Cobre apenas notas do Rio de Janeiro; outros estados ficam fora da v1.
