# ADR 0004: Autenticação por sessão com cookie

|            |            |
| ---------- | ---------- |
| **Status** | Aceita     |
| **Data**   | 07/11/2026 |

## Contexto

O sistema guarda dados pessoais de compras e tem um frontend (React) separado do backend (Spring Boot). Precisa de login, logout, troca de senha e exclusão de conta (RF18), com os dados de cada usuário isolados. A versão 1 tem poucos usuários, um único servidor e nenhum aplicativo de celular nativo.

O servidor não lembra de um pedido para o outro, então depois do login o navegador precisa apresentar algo que prove quem é o usuário em cada pedido.

## Decisão

Usar **sessão no servidor com cookie**:

- O servidor guarda a sessão e entrega ao navegador um cookie com um identificador aleatório.
- O cookie é `HttpOnly` (o JavaScript da página não consegue lê-lo), `SameSite=Lax` e `Secure` em produção.
- A proteção contra CSRF fica ativada: o frontend envia um token em um cabeçalho nos pedidos que alteram dados.
- As sessões ficam na memória do servidor na versão 1.
- As senhas continuam guardadas com hash BCrypt.

## Alternativas consideradas

| Alternativa                                         | Por que não                                                                                                                                                      |
| --------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| JWT guardado no `localStorage`                      | Um script malicioso na página consegue roubar o token (XSS). Cancelar um acesso antes do vencimento é difícil. Exige mais código (geração, validação, renovação) |
| JWT em cookie `HttpOnly`                            | Resolve o roubo por script, mas passa a ter os mesmos cuidados do cookie (CSRF) somados à complexidade do JWT                                                    |
| Sessão guardada no PostgreSQL (Spring Session JDBC) | Útil se houver mais de um servidor ou se for preciso manter o login após reiniciar. Fica adiada: não é necessária na versão 1                                    |

## Consequências

**Positivas**

- O cookie `HttpOnly` protege o identificador contra roubo por script.
- Sair, trocar a senha ou excluir a conta derruba a sessão na hora.
- Menos código para manter do que com JWT.

**Negativas e cuidados**

- Exige proteção CSRF e o envio do token pelo frontend.
- Frontend e API precisam ficar no mesmo site. No desenvolvimento, o proxy do Vite resolve. No deploy, será preciso servir os dois no mesmo domínio ou atrás de um proxy, porque navegadores modernos tendem a bloquear cookies de terceiros.
- Sessões na memória são perdidas quando o servidor reinicia (o usuário faz login de novo) e não funcionam com vários servidores sem Spring Session.

**Quando revisar:** aplicativo de celular nativo, mais de um servidor ou uma API usada por outros sistemas.
