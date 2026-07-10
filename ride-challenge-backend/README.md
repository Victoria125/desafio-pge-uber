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

## Arquitetura

- `config-server`: centraliza configurações dos serviços.
- `eureka-server`: service discovery.
- `api-gateway`: ponto único de entrada HTTP em `localhost:8080`.
- `account-service`: cadastro e consulta de contas de cliente/motorista.
- `ride-service`: criação, edição, listagem, aceite, status, timeout e notificações de corridas.
- `postgres`: persistência de contas e corridas.
- `kafka`: fila de pedidos de corrida.
- `redis`: cache de status das corridas.

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
```

Buscar conta por id:

```http
GET /accounts/{id}
```

### Corridas

Criar corrida:

```http
POST /rides
Content-Type: application/json
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
```

Buscar corrida por id:

```http
GET /rides/{id}
```

Consultar status:

```http
GET /rides/{id}/status
```

Aceitar corrida:

```http
POST /rides/{id}/accept
Content-Type: application/json
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

Windows:

```bash
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Os testes cobrem cenários funcionais e de erro dos casos de uso principais, incluindo criação de conta, criação de corrida, edição de rota, aceite, consulta de status e timeout.

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
2. Criar uma conta `CLIENT`.
3. Criar uma conta `DRIVER`.
4. Criar uma corrida com o id do cliente.
5. Opcionalmente editar origem e destino antes da corrida ser finalizada.
6. Ver a mensagem publicada na fila Kafka e enviada via WebSocket.
7. Aceitar a corrida com o id do motorista.
8. Consultar a corrida e verificar status `IN_PROGRESS` com o motorista vinculado.