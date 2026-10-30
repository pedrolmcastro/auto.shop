CREATE TABLE identity.sessao (
    id UUID PRIMARY KEY,
    usuario UUID NOT NULL REFERENCES identity.usuario (id),
    criada_em TIMESTAMP WITH TIME ZONE NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogada_em TIMESTAMP WITH TIME ZONE
);
