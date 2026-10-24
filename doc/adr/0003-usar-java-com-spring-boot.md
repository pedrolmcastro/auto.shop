# ADR 3: Usar Java com Spring Boot

- Autor: Pedro Lucas de Moliner de Castro
- Data: 2026-10-24
- Status: Accepted

## Contexto

É necessário iniciar o desenvolvimento do MVP do backend que fornece a API do novo sistema da oficina. O time precisa
definir qual linguagem de programação e ferramentas auxiliares serão utilizadas no desenvolvimento.

## Decisão

Usar a linguagem Java 25 com framework Spring Boot 4. Atualizações para novas versões LTS da linguagem e do framework
serão constantes e graduais para evitar grandes migrações.

### Justificativa

- Familiaridade do time com a linguagem, o framework e o ecossistema
- Ecossistema maduro, estável e com ferramentas bem estabelecidas
- Necessidade de adoção de um ecossistema familiar para desenvolvimento veloz do MVP
- Limitações de performance conhecidas desse ecossistema não têm impacto relevante comprovado no sistema desenvolvido

### Consequências

#### Positivas

- Adoção de um ecossistema padrão e amplamente utilizado
- Alta velocidade de desenvolvimento devido às facilidades do ecossistema e familiaridade do time
- Facilidade de integração de futuros novos membros do time dado o uso de ferramentas padrão

#### Negativas

- Problemas conhecidos de performance como alto tempo de inicialização e pausas para garbage collection

## Alternativas rejeitadas

### 1: Usar Java com outros frameworks como Micronaut

**Vantagens:**

- Possibilidade de uso de frameworks teoricamente mais performáticos, principalmente com relação à inicialização

**Desvantagens:**

- Baixa familiaridade do time
- Ecossistemas menos amplamente utilizados

**Justificativa da rejeição:**

- Baixa familiaridade do time
- Potencial ganho de performance sem impacto relevante comprovado no sistema desenvolvido

### 2: Usar Java sem um framework web

**Vantagens:**

- Controle completo da implementação
- Possibilidade de ganhos de desempenho significativos dada uma implementação mínima especializada

**Desvantagens:**

- Alto custo de implementação inicial
- Alto custo de manutenibilidade
- Alto risco de introdução de vulnerabilidades de segurança
- Curva de aprendizado elevada para futuros novos membros do time

**Justificativa da rejeição:**

- Alto custo de implementação e manutenibilidade
- Alto risco de introdução de vulnerabilidades de segurança
- Ausência de provas da necessidade de implementação de otimizações especializadas

### 3: Usar outra linguagem de programação

**Vantagens:**

- Possibilidade de uso de linguagens de sistema como C++, Rust ou Zig para performance
- Possibilidade de uso de linguagens mais modernas e menos verbosas como Swift ou Kotlin

**Desvantagens:**

- Baixa familiaridade do time, principalmente com os ecossistemas das outras linguagens

**Justificativa da rejeição:**

- Baixa familiaridade do time
- Potencial ganho de performance por controle manual de memória sem impacto relevante comprovado no sistema desenvolvido
- Possibilidade de uso de uma versão recente do Java com novos frameworks atenua vantagens de outras linguagens novas
