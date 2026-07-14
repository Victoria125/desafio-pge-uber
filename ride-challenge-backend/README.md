# Ride Challenge Backend

Backend do desafio de corridas implementado em Java 17 com Spring Boot e Spring Cloud.

A aplicação foi organizada em microsserviços para separar configuração, descoberta, gateway, contas e corridas. O fluxo principal permite criar contas de cliente/motorista, criar corridas, publicar o pedido em uma fila Kafka, notificar motoristas via WebSocket e aceitar a corrida.

## Tecnologias

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Config Server
- Eureka Server
- PostgreSQL
- Kafka
- Redis
- WebSocket/STOMP
- Docker Compose
- JUnit e Mockito
- Testcontainers
- JaCoCo

## Arquitetura

- `config-server`: centraliza configurações dos serviços.
- `eureka-server`: service discovery.
- `api-gateway`: ponto único de entrada HTTP em `localhost:8080`.
- `account-service`: cadastro e consulta de contas de cliente/motorista.
- `ride-service`: criação, edição, listagem, aceite, status, timeout e notificações de corridas.
- `postgres`: persistência de contas e corridas.
- `kafka`: fila de pedidos de corrida.
- `redis`: cache de status das corridas.
- `frontend`: aplicação Angular servida por nginx em `localhost:4200`.

## Como rodar com Docker

Pré-requisitos:

- Docker
- Docker Compose

Na pasta `ride-challenge-backend`, execute:

```bash
docker compose up --build
```

Para rodar em segundo plano:

```bash
docker compose up -d --build
```

O compose sobe toda a infraestrutura, os microsserviços e o frontend, respeitando a ordem de inicialização via healthchecks (`depends_on` com `condition: service_healthy`).

Frontend:

```text
http://localhost:4200
```

Gateway da API:

```text
http://localhost:8080
```

Eureka:

```text
http://localhost:8761
```

PostgreSQL exposto localmente:

```text
localhost:5433
```

Kafka exposto localmente:

```text
localhost:9092
```

## Endpoints principais

Todas as rotas abaixo devem ser acessadas pelo gateway em `http://localhost:8080`.

### Autenticacao

Criar conta e login sao publicos. As demais rotas HTTP passam pelo gateway e exigem:

```http
Authorization: Bearer <token>
```

Apos validar o token, o gateway extrai a identidade das claims e a propaga para os
servicos internos nos headers `X-User-Id` e `X-User-Type` (removendo qualquer valor
enviado pelo cliente, para evitar spoofing). O ride-service usa esses headers para
autorizar as operacoes:

- `POST /rides` e `PUT /rides/{id}`: apenas `CLIENT`, e o `userId` do body deve ser o proprio usuario autenticado.
- `POST /rides/{id}/accept`: apenas `DRIVER`, e o `driverId` do body deve ser o proprio motorista autenticado.

Violacoes retornam `403 Forbidden`; requisicoes sem os headers de identidade (fora do gateway) retornam `401 Unauthorized`.

O segredo usado para assinar/validar o JWT pode ser sobrescrito pela variavel de
ambiente `SECURITY_JWT_SECRET` (no docker-compose, basta exportar `JWT_SECRET` antes
do `docker compose up`). Sem a variavel, um valor padrao de desenvolvimento e usado.

Login:

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "email": "ana@example.com",
  "password": "secret123"
}
```

Resposta:

```json
{
  "token": "jwt...",
  "expiresIn": 86400,
  "accountId": "id-da-conta",
  "name": "Ana Cliente",
  "email": "ana@example.com",
  "type": "CLIENT"
}
```

### Contas

Criar conta:

```http
POST /accounts
Content-Type: application/json
```

```json
{
  "name": "Ana Cliente",
  "email": "ana@example.com",
  "password": "secret123",
  "type": "CLIENT"
}
```

Tipos aceitos:

```text
CLIENT
DRIVER
```

Listar contas:

```http
GET /accounts
Authorization: Bearer <token>
```

Buscar conta por id:

```http
GET /accounts/{id}
Authorization: Bearer <token>
```

### Corridas

Criar corrida:

```http
POST /rides
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "userId": "id-do-cliente",
  "startAddress": "Unifor",
  "destinationAddress": "Praia de Iracema"
}
```

Editar origem e destino de uma corrida não finalizada:

```http
PUT /rides/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "userId": "id-do-cliente",
  "startAddress": "Nova origem",
  "destinationAddress": "Novo destino"
}
```

Listar corridas:

```http
GET /rides
Authorization: Bearer <token>
```

Buscar corrida por id:

```http
GET /rides/{id}
Authorization: Bearer <token>
```

Consultar status:

```http
GET /rides/{id}/status
Authorization: Bearer <token>
```

Aceitar corrida:

```http
POST /rides/{id}/accept
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "driverId": "id-do-motorista"
}
```

## WebSocket

Endpoint STOMP:

```text
ws://localhost:8080/ws
```

Tópico usado para notificar motoristas:

```text
/topic/rides
```

Quando uma corrida é criada, o `ride-service` publica uma mensagem no Kafka. O consumer lê a mensagem e notifica os motoristas conectados pelo tópico `/topic/rides`. Quando uma corrida aberta é editada, o mesmo tópico recebe a rota atualizada.

## Timeout de corridas

Corridas criadas e não aceitas são verificadas por um job agendado. Por padrão:

```text
ride.timeout.seconds=120
ride.timeout.check-interval-ms=30000
```

Quando expiram, elas são marcadas como `CANCELLED` e uma notificação é enviada pelo WebSocket.

## Testes

Na pasta `ride-challenge-backend`, execute:

Testes unitários (rápidos, sem Docker):

```bash
./mvnw test
```

Build completo com testes de integração e gate de cobertura (precisa de Docker):

```bash
./mvnw clean verify
```

No Windows, use `.\mvnw.cmd` no lugar de `./mvnw`. Para pular os testes de integração em máquinas sem Docker, use `-DskipITs`.

Os testes unitários cobrem cenários funcionais e de erro dos casos de uso principais, incluindo criação de conta, criação de corrida, edição de rota, aceite, consulta de status e timeout. Os testes de integração validam os gateways de persistência contra um PostgreSQL real via Testcontainers. O JaCoCo falha o build se a cobertura de domínio e aplicação ficar abaixo de 90%, e o relatório fica em `*/target/site/jacoco/index.html`.

A estratégia completa de testes está documentada em [`TESTING.md`](../TESTING.md). Há também uma collection do Postman com o fluxo completo em [`postman/`](../postman/). O pipeline de CI em `.github/workflows/ci.yml` roda tudo isso a cada push.

## Tratamento de erros

Os serviços possuem tratamento global de exceções com respostas HTTP apropriadas:

- `400 Bad Request`: validação ou argumento inválido.
- `404 Not Found`: recurso inexistente.
- `409 Conflict`: corrida já aceita/finalizada.
- `500 Internal Server Error`: erro inesperado.

As respostas seguem o formato:

```json
{
  "errors": {
    "campo": "mensagem de erro"
  }
}
```

## Fluxo demonstrável

1. Subir os containers com `docker compose up --build`.
2. Criar uma conta `CLIENT` com senha.
3. Fazer login com a conta `CLIENT` e guardar o token JWT.
4. Criar uma conta `DRIVER` com senha.
5. Fazer login com a conta `DRIVER` e guardar o token JWT.
6. Criar uma corrida com o id do cliente usando `Authorization: Bearer <token-do-cliente>`.
7. Opcionalmente editar origem e destino antes da corrida ser finalizada.
8. Ver a mensagem publicada na fila Kafka e enviada via WebSocket.
9. Aceitar a corrida com o id do motorista usando `Authorization: Bearer <token-do-motorista>`.
10. Consultar a corrida e verificar status `IN_PROGRESS` com o motorista vinculado.
