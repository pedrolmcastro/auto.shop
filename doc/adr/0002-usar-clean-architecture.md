# ADR 2: Usar Clean Architecture

- Autor: Pedro Lucas de Moliner de Castro
- Data: 2026-10-23
- Status: Accepted

## Contexto

Dada a definição da [ADR 1](./0001-usar-arquitetura-monolitica-modular.md), resta definir se haverá ou não um padrão de
estruturação de código dentro de cada módulo e, se sim, qual será

## Decisão

Utilizar Clean Architecture dentro de cada módulo com a seguinte estrutura:

```text
├── domain/         # independente
├── application/    # depende do domain
├── infrastructure/ # depende da application e do domain
└── presentation/   # depende da application
```

### Justificativa

- Facilidade de compreensão entre módulos e colaboração devido à padronização
- Familiaridade do time com o padrão
- Facilidade no desenvolvimento de testes
- Facilidade de migrações futuras
- Manutenibilidade futura alta
- Já existe uma divisão "vertical" de funcionalidades com alta coesão nos módulos

### Consequências

#### Positivas

- Mantém padronização entre diferentes módulos
- Clareza na definição de regras de negócio e fluxos de caso de uso muito alta
- Facilita o desenvolvimento de testes consistentes e independentes
- Abertura para potenciais migrações de infraestrutura como de banco de dados ou suporte a um novo protocolo de API

#### Negativas

- Velocidade inicial de desenvolvimento reduzida
- Introduz uma quantidade significativa de boilerplate
- Introduz complexidade de navegação no projeto
- Curva de aprendizado elevada para novos integrantes do time no futuro

## Alternativas rejeitadas

### 1: Não usar um padrão de arquitetura estruturada

**Vantagens:**

- Velocidade inicial de desenvolvimento muito alta
- Não introduz boilerplate relacionado a estrutura
- Liberdade total de uso de estruturas específicas para cada módulo

**Desvantagens:**

- Dificuldade de compreensão devido à falta de padronização
- Clareza na definição de regras de negócio e fluxos de caso de uso muito baixa
- Alto risco de alto acoplamento completo
- Dificuldade de desenvolvimento de testes consistentes e independentes
- Manutenibilidade futura muito prejudicada

**Justificativa da rejeição:** Alta penalidade na manutenibilidade futura e dificuldade de compreensão e colaboração

### 2: Usar Layered Architecture convencional

**Vantagens:**

- Velocidade inicial de desenvolvimento alta
- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática

**Desvantagens:**

- Risco de diluição e mescla de regras de negócio com lógica de infraestrutura diminuindo clareza
- Alto risco de acoplamento entre lógicas de negócio e persistência no banco de dados
- Dificuldade de isolamento de testes de regra de negócio com relação a camada de persistência
- Dificuldade de migração de infraestrutura de base como banco de dados

**Justificativa da rejeição:** Dificuldade de desenvolvimento de testes e alto acoplamento com o banco de dados

### 3: Usar outra arquitetura com dependência invertida como Hexagonal ou Onion

**Vantagens:**

- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática

**Desvantagens:**

- Baixa familiaridade do time
- Padronização menos rigorosa com conceitos mais abrangentes e potencial de disparidade entre módulos

**Justificativa da rejeição:** Baixa familiaridade do time e padronização menos rigorosa

### 4: Usar Vertical Slice Architecture

**Vantagens:**

- Velocidade de desenvolvimento alta
- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática

**Desvantagens:**

- Baixa familiaridade do time
- Dificuldade de manter consistência de regras de negócio entre casos de uso diferentes
- Alto risco de duplicação de código
- Ganho de coesão tem um impacto atenuado devido à divisão pré-existente dos módulos coesos

**Justificativa da rejeição:** Baixa familiaridade do time e baixa consistência de regras de negócio
