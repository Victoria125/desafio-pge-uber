# Estratégia de testes e qualidade

Este projeto usa uma pirâmide de testes com gate de cobertura no build — inspirada
no fluxo de qualidade usado em pipelines corporativos (testes → cobertura → gate → deploy).

## Backend (Java / Spring Boot)

| Camada | Ferramenta | O que cobre |
|---|---|---|
| Unidade | JUnit 5 + Mockito (Surefire) | Use cases e entidades de domínio, com gateways mockados |
| Integração | Testcontainers + Postgres real (Failsafe) | Gateways de persistência e a query customizada do job de timeout |
| Cobertura | JaCoCo com `check` | Gate de **90% de linhas** sobre domínio + aplicação; falha o build abaixo disso |

```bash
cd ride-challenge-backend

# unitários apenas (rápido, sem Docker)
./mvnw test

# build completo: unitários -> integração (precisa de Docker) -> gate de cobertura
./mvnw clean verify

# pular os testes de integração (ex.: máquina sem Docker)
./mvnw clean verify -DskipITs
```

O relatório de cobertura fica em `*/target/site/jacoco/index.html`.

Convenção de nomes: `*Test.java` = unitário (Surefire, fase `test`);
`*IT.java` = integração (Failsafe, fase `integration-test`).

**Por que Testcontainers em vez de H2?** O banco de produção é Postgres; um banco
em memória diverge em tipos, DDL e comportamento de query. O teste de integração
sobe um `postgres:16-alpine` descartável via Docker e valida o mapeamento JPA e a
query `findTop50ByStatusAndCreatedAtBefore` contra o banco real.

**Por que o gate exclui `infrastructure/`?** O gate mede regra de negócio
(domínio + use cases), onde cobertura baixa esconde bug. Controllers, configs e
adapters são exercitados pelos testes de integração e E2E — cobri-los com
unitários de mock seria cobertura de vaidade.

## Frontend (Angular)

| Camada | Ferramenta | O que cobre |
|---|---|---|
| Unidade | Jasmine + Karma | Componentes e serviços |
| E2E (smoke) | Cypress | Fluxo de entrada: redirect de sessão, login, filtro por perfil |

```bash
cd ride-challenge-frontend

npm test               # unitários
npm run e2e            # Cypress interativo (precisa do `npm start` em outro terminal)
npm run e2e:run        # Cypress headless
```

O smoke E2E stuba a API com `cy.intercept`, então roda sem backend de pé.

## API (Postman)

Importe `postman/ride-challenge.postman_collection.json`. A collection percorre o
fluxo completo via API Gateway (`http://localhost:8080`) e encadeia os IDs
automaticamente: criar cliente → criar motorista → criar corrida → aceitar → status.

## CI (GitHub Actions)

`.github/workflows/ci.yml` roda em todo push/PR na `main`:

- **backend** — `mvn verify` (unitários + Testcontainers + gate JaCoCo de 90%)
- **frontend** — testes unitários headless + build de produção

O gate de cobertura no `verify` cumpre o papel de quality gate do pipeline:
cobertura abaixo do mínimo bloqueia o merge, como um SonarQube faria no deploy.
