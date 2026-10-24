# ADR 1: Usar arquitetura monolítica modular

- Autor: Pedro Lucas de Moliner de Castro
- Data: 2026-10-23
- Status: Accepted

## Contexto

É necessário desenvolver um novo sistema de gerenciamento de clientes, veículos, peças e serviços para uma oficina de
médio porte. Inicialmente, será construído apenas um MVP da API, mas espera-se que essa implementação evolua e cresça
futuramente e sustente o uso em produção. É necessário estabelecer o padrão arquitetural de alto nível a ser aplicado

## Decisão

Utilização de uma arquitetura monolítica modular

### Justificativa

- A entrega inicial de um MVP exige desenvolvimento rápido e com simplicidade operacional
- A necessidade de evolução e a intenção de uso em produção requerem facilidade de manutenção futura
- O porte da oficina pode ser atendido por um serviço único, sem necessidade de grande escalabilidade inicial
- O time de desenvolvimento é pequeno e não requer deployment independente para ser ágil inicialmente
- Os objetivos do projeto não fogem da normalidade e não requerem a aplicação de tecnologias muito especializadas
- Ferramentas de definição e validação de limites de módulos existem e são amplamente utilizadas no mercado

### Consequências

#### Positivas

- Organização estrutural clara
- Simplicidade operacional para deployment, monitoramento e depuração
- Comunicação entre módulos simples e performática
- Permite migração futura gradual para microsserviços

#### Negativas

- Falhas podem facilmente afetar o sistema inteiro
- Escalabilidade interdependente
- Deployment acoplado
- Dependência tecnológica entre módulos
- Requer planejamento prévio da arquitetura e dos contratos
- Requer disciplina rígida e verificação de limites de módulos

## Alternativas rejeitadas

### 1: Arquitetura monolítica tradicional

**Vantagens:**

- Complexidade de implementação muito baixa
- Velocidade inicial de desenvolvimento muito alta
- Não requer planejamento prévio da arquitetura e dos contratos
- Não requer validação de limites de módulos ou serviços

**Desvantagens:**

- Baixa organização estrutural
- Manutenibilidade futura muito prejudicada
- Difícil migração futura para monólitos modulares ou microsserviços

**Justificativa da rejeição:** Grande penalidade na manutenibilidade futura e dificuldade de migração

### 2: Arquitetura de microsserviços

**Vantagens:**

- Isolamento de falhas
- Escalabilidade granular
- Facilidade na implementação de redundância por serviço
- Independência de deployments
- Independência tecnológica entre serviços
- Facilidade de aplicação e garantia de limites entre serviços

**Desvantagens:**

- Complexidade de implementação alta
- Complexidade operacional alta para deployment, monitoramento e depuração
- Comunicação entre serviços complexa, sujeita a falhas e com penalidade de desempenho

**Justificativa da rejeição:** Complexidade de implementação alta sem aplicação imediata das vantagens de escalabilidade
