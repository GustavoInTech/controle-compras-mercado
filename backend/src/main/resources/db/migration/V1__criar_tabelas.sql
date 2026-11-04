-- Tabelas iniciais do sistema (ver docs/03-modelo-de-dados.md)
-- Dinheiro sempre em NUMERIC, nunca em FLOAT/DOUBLE.

CREATE TABLE usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    meta_mensal NUMERIC(12, 2),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- O e-mail é único sem diferenciar maiúsculas de minúsculas
CREATE UNIQUE INDEX ux_usuario_email ON usuario (lower(email));

-- Uma loja, identificada pelo CNPJ completo. Dado público, compartilhado entre usuários.
CREATE TABLE mercado (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    endereco VARCHAR(300),
    CONSTRAINT ck_mercado_cnpj CHECK (cnpj ~ '^[0-9]{14}$')
);

CREATE TABLE categoria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(60) NOT NULL UNIQUE
);

-- Produto "de verdade" de um usuário: agrupa itens com nomes diferentes
CREATE TABLE produto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    categoria_id BIGINT REFERENCES categoria (id),
    nome VARCHAR(200) NOT NULL,
    conteudo_quantidade NUMERIC(12, 3),
    conteudo_unidade VARCHAR(10),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_produto_usuario ON produto (usuario_id);

-- Memória do sistema: "nesta loja, este nome normalizado é este produto"
CREATE TABLE produto_apelido (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    mercado_id BIGINT NOT NULL REFERENCES mercado (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id) ON DELETE CASCADE,
    nome_normalizado VARCHAR(300) NOT NULL,
    CONSTRAINT uq_apelido UNIQUE (
        usuario_id,
        mercado_id,
        nome_normalizado
    )
);

-- Uma compra. O "identificador" evita nota duplicada: a chave de 44 dígitos;
-- se não vier, cnpj|serie|numero; se também faltar, uma impressão digital (hash).
CREATE TABLE nota (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    mercado_id BIGINT NOT NULL REFERENCES mercado (id),
    identificador VARCHAR(64) NOT NULL,
    chave_acesso VARCHAR(44),
    data_compra DATE NOT NULL,
    valor_total NUMERIC(12, 2) NOT NULL,
    desconto NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_a_pagar NUMERIC(12, 2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_nota_usuario_identificador UNIQUE (usuario_id, identificador),
    CONSTRAINT ck_nota_chave CHECK (
        chave_acesso IS NULL
        OR chave_acesso ~ '^[0-9]{44}$'
    ),
    CONSTRAINT ck_nota_valores CHECK (
        valor_total >= 0
        AND desconto >= 0
        AND valor_a_pagar >= 0
    )
);

CREATE INDEX ix_nota_usuario_data ON nota (usuario_id, data_compra);

-- Cada linha da nota, com o nome exatamente como veio e o nome normalizado
CREATE TABLE item_nota (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nota_id BIGINT NOT NULL REFERENCES nota (id) ON DELETE CASCADE,
    produto_id BIGINT REFERENCES produto (id) ON DELETE SET NULL,
    nome_original VARCHAR(300) NOT NULL,
    nome_normalizado VARCHAR(300) NOT NULL,
    codigo_mercado VARCHAR(40),
    quantidade NUMERIC(12, 3) NOT NULL,
    unidade VARCHAR(10) NOT NULL,
    preco_unitario NUMERIC(12, 4) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    CONSTRAINT ck_item_valores CHECK (
        quantidade > 0
        AND preco_unitario >= 0
        AND total >= 0
    )
);

CREATE INDEX ix_item_nota_nota ON item_nota (nota_id);

CREATE INDEX ix_item_nota_produto ON item_nota (produto_id);