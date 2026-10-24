# ADR 2: Usar Clean Architecture

- Autor: Pedro Lucas de Moliner de Castro
- Data: 2026-10-23
- Status: Accepted

## Contexto

Dada a definição da [ADR 0001](./0001-usar-arquitetura-monolitica-modular.md), resta definir se haverá ou não um padrão
de estruturação de código dentro de cada módulo e, em caso afirmativo, qual será. O tamanho pequeno do time e a entrega
de um MVP inicialmente exigem flexibilidade em termos de permitir que desenvolvedores sejam direcionados para trabalhar
em outros módulos durante o desenvolvimento. A resiliência e corretude do sistema são de grande importância à oficina e
por isso o projeto requer facilidade de implantação de testes automatizados estáveis e independentes

## Decisão

Utilizar Clean Architecture dentro de cada módulo.

### Justificativa

- Facilidade de compreensão da estrutura dos módulos e de colaboração, devido à padronização
- Familiaridade do time com o padrão arquitetural
- Familiaridade do time e intenção de utilização de práticas de DDD
- Facilidade no desenvolvimento de testes pela utilização de inversão de dependências
- Maior facilidade de substituição de ferramentas de infraestrutura devido ao baixo acoplamento
- Facilidade de manutenção a longo prazo
- Já existe uma divisão "vertical" de funcionalidades com alta coesão a nível de módulos
- Estruturação interna dos módulos não precisa ser descartada em caso de migração para microsserviços
- Custo de complexidade e introdução de boilerplate considerados aceitáveis dado o ganho de manutenibilidade

### Consequências

#### Positivas

- Mantém padronização entre diferentes módulos
- Sinergia com práticas de DDD
- Clareza na definição de regras de negócio e fluxos de caso de uso muito alta
- Facilita o desenvolvimento de testes consistentes e independentes
- Facilita possíveis migrações de banco de dados ou adoção de novos protocolos de API

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
- Alto risco de acoplamento
- Dificuldade de desenvolvimento de testes consistentes e independentes
- Facilidade de manutenção futura muito prejudicada

**Justificativa da rejeição:**

- Alta penalidade na manutenibilidade futura
- Dificuldade de compreensão e colaboração entre diferentes módulos

### 2: Usar Layered Architecture convencional

**Vantagens:**

- Velocidade inicial de desenvolvimento alta
- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática

**Desvantagens:**

- Risco de diluição e mistura das regras de negócio com a lógica de infraestrutura, o que reduz a clareza
- Sinergia menor com práticas de DDD
- Alto risco de acoplamento entre a lógica de negócio e a persistência no banco de dados
- Dificuldade para desenvolver testes de regras de negócio isolados da camada de persistência
- Dificuldade para migrar componentes básicos da infraestrutura, como o banco de dados

**Justificativa da rejeição:**

- Dificuldade de desenvolvimento de testes
- Risco de alto acoplamento com o banco de dados

### 3: Usar outra arquitetura com dependência invertida como Hexagonal ou Onion

**Vantagens:**

- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática

**Desvantagens:**

- Baixa familiaridade do time
- Convenções menos prescritivas a com conceitos mais abrangentes e potencial de disparidade entre módulos

**Justificativa da rejeição:**

- Baixa familiaridade do time
- Convenções menos prescritivas

### 4: Usar Vertical Slice Architecture

**Vantagens:**

- Velocidade de desenvolvimento alta
- Introduz uma quantidade menor de boilerplate
- Curva de aprendizado menos dramática
- Alta coesão do código para cada caso de uso

**Desvantagens:**

- Baixa familiaridade do time
- Pouca sinergia com práticas de DDD
- Dificuldade de manter consistência de regras de negócio entre casos de uso diferentes
- Alto risco de duplicação de código

**Justificativa da rejeição:**

- Baixa familiaridade do time
- Risco de aplicação inconsistente de regras de negócio entre casos de uso
- Ganho de coesão tem impacto atenuado devido à divisão preexistente dos módulos
