# Ride Challenge Frontend

Frontend do desafio de corridas implementado em Angular com PrimeNG.

A aplicação consome a API REST do backend pelo API Gateway, usa WebSocket/STOMP para receber corridas em tempo real e possui testes de interface com Jasmine e Karma, seguindo o exemplo sugerido no enunciado do desafio.

## Tecnologias

- Angular
- PrimeNG
- Reactive Forms
- HttpClient
- STOMP WebSocket com `@stomp/rx-stomp`
- Jasmine
- Karma
- ChromeHeadless para testes automatizados
- Cypress para testes E2E

## Pré-requisitos

- Node.js compatível com o projeto
- npm
- Backend rodando em `http://localhost:8080`

## Configuração da API

As URLs ficam em `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'ws://localhost:8080/ws',
} as const;
```

A API REST é consumida via `HttpClient` usando o token `API_BASE_URL`. As rotas ficam centralizadas em `src/app/core/api/api-routes.ts`.

## Como rodar

Na pasta `ride-challenge-frontend`, instale as dependências:

```bash
npm install
```

Rode o frontend:

```bash
npm start
```

Acesse:

```text
http://localhost:4200
```

## Como testar

Rodar os testes de interface com Jasmine/Karma:

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

O projeto possui testes para:

- renderizar corridas do cliente logado;
- impedir envio de formulário inválido;
- criar corrida com o id do cliente logado;
- editar origem e destino de corrida não finalizada com o id do cliente logado;
- manter a lista atual quando a criação falha;
- listar apenas corridas disponíveis para motorista;
- aceitar corrida com o id do motorista logado;
- recarregar corridas quando o aceite falha;
- receber corrida via WebSocket;
- exibir toast de erro claro para falhas da API.

Rodar os smoke tests E2E com Cypress (com o `npm start` rodando em outro terminal):

```bash
npm run e2e:run
```

Os smoke tests cobrem o redirect de visitante sem sessão para o login, a renderização e o filtro de contas por perfil e a entrada como cliente até a tela de corridas. A API é stubada com `cy.intercept`, então rodam sem backend de pé.

## Build

```bash
npm run build
```

Os arquivos de produção são gerados em `dist/ride-challenge-frontend`.

## Docker

O frontend possui um `Dockerfile` multi-stage (build Node + nginx) e faz parte do `docker-compose.yml` do backend. Ao subir o compose, a aplicação fica disponível em `http://localhost:4200` servida por nginx, sem precisar de `npm start`.

Para buildar a imagem isoladamente:

```bash
docker build -t ride-frontend .
```

## Fluxo da aplicação

1. Na tela de login, escolha ou crie uma conta.
2. Use tipo `CLIENT` para criar corridas.
3. Use tipo `DRIVER` para visualizar corridas disponíveis.
4. Cliente informa origem e destino.
5. Frontend chama `POST /rides` no backend.
6. Enquanto a corrida não estiver finalizada, o cliente pode clicar no ícone de edição e alterar origem/destino via `PUT /rides/{id}`.
7. Backend publica a corrida na fila Kafka.
8. Motoristas recebem notificação em tempo real via WebSocket.
9. Motorista aceita a corrida.
10. Frontend chama `POST /rides/{id}/accept`.
11. Corrida fica vinculada ao motorista e muda para `IN_PROGRESS`.

## Telas principais

- Login e criação de conta.
- Corridas do cliente.
- Corridas disponíveis para motorista.
- Tela de acesso negado quando o perfil não tem permissão.

## Tratamento de erros

O frontend possui interceptor global para erros HTTP. Quando o backend retorna erro, a UI mostra um toast com mensagem clara para o usuário.

Exemplos:

- validação de formulário;
- recurso não encontrado;
- corrida já aceita por outro motorista;
- erro inesperado do servidor.

## Backend esperado

Antes de testar o frontend, suba o backend:

```bash
cd ../ride-challenge-backend
docker compose up --build
```

Com o backend de pé, o gateway deve responder em:

```text
http://localhost:8080
```