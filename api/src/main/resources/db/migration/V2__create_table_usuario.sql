CREATE TABLE identity.usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(30) NOT NULL,
    senha TEXT NOT NULL,
    CONSTRAINT usuario_nome_unique UNIQUE (nome)
);
