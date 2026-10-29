# ADR 0001: Não ler automaticamente o site da Fazenda

| | |
|---|---|
| **Status** | Aceita |
| **Data** | 27/10/2026 |

## Contexto

Toda NFC-e (nota fiscal de consumidor) tem um QR Code que abre uma página da SEFAZ-RJ com todos os dados da compra. A ideia inicial era o servidor do sistema abrir essa página e extrair os dados sozinho, sem nenhum trabalho para o usuário.

Antes de construir, foi feito um teste: um programa em Node.js (`fetch`) abrindo um link real de nota. O resultado foi um status 200, mas o conteúdo não era a nota. Era uma página da Secretaria de Fazenda informando que o serviço bloqueia acessos de certos endereços de IP a serviços com dados sujeitos a sigilo fiscal. A mesma nota abria normalmente no navegador, na mesma rede.

## Decisão

O servidor do sistema **não acessa o site da SEFAZ**, e **não será feita nenhuma tentativa de contornar o bloqueio**.

## Alternativas consideradas

| Alternativa | Resultado |
|---|---|
| Contornar o bloqueio (proxies, imitar navegador, automação de navegador) | **Rejeitada.** É uma barreira de segurança colocada de propósito pelo governo. Contorná-la traz risco jurídico, e o sistema guarda dados de compra de pessoas (LGPD). Além disso seria frágil: qualquer mudança no site derrubaria o sistema |
| Ler a página no aparelho do usuário (navegador embutido em app de celular) | **Adiada.** O acesso partiria do aparelho da pessoa, mas não foi testado se o site aceita esse tipo de leitura. Exige app de celular |
| O usuário copia o texto da nota e cola no sistema | **Escolhida.** Ver [ADR 0002](0002-entrada-por-copiar-e-colar.md) |
| Foto ou print da nota lido por OCR ou IA | **Adiada.** Ver ADR 0002 |

## Consequências

**Positivas**
- O sistema não depende de acessar um serviço que bloqueia acesso automatizado.
- Sem risco jurídico por burlar bloqueio.
- Os dados chegam por uma ação do próprio usuário, sobre a nota dele.

**Negativas**
- O usuário precisa de alguns passos manuais (escanear, copiar, colar).
- Não se sabe por que o navegador do usuário passa e o programa não; essa causa não foi investigada, e investigá-la para fins de contornar o bloqueio está fora de cogitação.

**Quando revisar:** se existir uma forma oficial e autorizada de consulta (por exemplo, uma API pública para esse fim).
