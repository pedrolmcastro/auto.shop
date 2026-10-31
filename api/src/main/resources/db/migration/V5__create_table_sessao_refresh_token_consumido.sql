CREATE TABLE identity.sessao_refresh_token_consumido (
    hash VARCHAR(64) PRIMARY KEY,
    sessao UUID NOT NULL REFERENCES identity.sessao (id),
    consumido_em TIMESTAMP WITH TIME ZONE NOT NULL
);
