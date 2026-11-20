-- Busca por nomes parecidos (similaridade de texto), usada nas sugestões de produto (ADR 0005)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Acelera a procura de apelidos parecidos com um nome novo
CREATE INDEX ix_apelido_nome_trgm ON produto_apelido USING gin (nome_normalizado gin_trgm_ops);