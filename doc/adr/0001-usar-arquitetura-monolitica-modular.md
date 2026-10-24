# ADR 1: Usar arquitetura monolítica modular

- Autor: Pedro Lucas de Moliner de Castro
- Data: 2026-10-23
- Status: Accepted

## Contexto

É necessário desenvolver um novo sistema integrado de atendimento e execução de serviços para a oficina. Atualmente, a
oficina é de médio porte, mas há expectativa de grande crescimento com a implantação do sistema, dado que hoje o maior
gargalo nas entregas é o gerenciamento desorganizado e muito manual. A estratégia de implementação é começar com o MVP
do back-end da API REST HTTP, mas é esperado que essa implementação evolua e seja reutilizada no futuro em produção. O
sistema deve suportar as seguintes funcionalidades desde o início:

- Gerenciamento CRUD administrativo de clientes
- Gerenciamento CRUD administrativo de veículos
- Gerenciamento CRUD administrativo de peças e insumos
- Controle administrativo de estoque de peças e insumos
- Gerenciamento CRUD administrativo de serviços
- Listagem administrativa de ordens de serviço
- Cálculo automático do orçamento associado à ordem de serviço
- Envio do orçamento para confirmação do cliente
- Acompanhamento do status do serviço por parte do cliente

## Decisão

Utilização de uma arquitetura monolítica modular. A divisão exata dos módulos está fora do escopo dessa ADR

### Justificativa

- A entrega inicial de um MVP exige desenvolvimento rápido e com simplicidade operacional
- A necessidade de evolução e a intenção de uso em produção requerem facilidade de manutenção futura
- O volume atual de operações da oficina pode ser atendido por um serviço único e inicialmente escalado como unidade
- A projeção de crescimento exige planejamento para a necessidade de escalabilidade maior no futuro
- O time de desenvolvimento é pequeno e não requer deployment independente para ser ágil inicialmente
- Os requisitos do sistema não justificam a necessidade de independência tecnológica entre módulos
- Ferramentas de definição e validação de limites de módulos existem e são amplamente utilizadas no mercado

### Consequências

#### Positivas

- Organização estrutural clara com limites e contratos bem definidos
- Simplicidade operacional para deployment, monitoramento e depuração
- Comunicação entre módulos simples, direta, sem chamadas de rede e com menor complexidade operacional
- Limites e contratos explícitos podem reduzir o esforço de isolar módulos em microsserviços em caso de migração futura

#### Negativas

- Módulos compartilham um processo e recursos, então falhas não tratadas e picos de consumo podem afetar todo o sistema
- O sistema inteiro é escalado como uma unidade
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

**Justificativa da rejeição:**

- Grande penalidade na manutenibilidade futura
- Dificuldade de migração

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

**Justificativa da rejeição:**

- Complexidade de implementação alta
- Não há necessidade imediata de grande escalabilidade no MVP
