# Ride Challenge Frontend

Frontend do desafio de corridas implementado em Angular com PrimeNG.

A aplicacao consome a API REST pelo API Gateway, usa WebSocket/STOMP para receber corridas em tempo real e possui testes de interface com Jasmine/Karma e smoke tests com Cypress.

## Tecnologias

- Angular
- PrimeNG
- Reactive Forms
- HttpClient
- STOMP WebSocket com `@stomp/rx-stomp`
- Jasmine
- Karma
- ChromeHeadless
- Cypress

## Pre-requisitos

- Node.js compativel com o projeto
- npm
- Backend rodando em `http://localhost:8080`

## Configuracao da API

As URLs ficam em `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'ws://localhost:8080/ws',
} as const;
```

A API REST e consumida via `HttpClient` usando o token `API_BASE_URL`. As rotas ficam centralizadas em `src/app/core/api/api-routes.ts`.

O login usa `POST /auth/login`. O token JWT retornado fica salvo na sessao local com expiracao e e enviado nas chamadas REST pelo header:

```http
Authorization: Bearer <token>
```

O WebSocket envia o mesmo token na query string `access_token`, para que o gateway valide o handshake.

## Como rodar

Na pasta `ride-challenge-frontend`, instale as dependencias:

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
- impedir envio de formulario invalido;
- criar corrida com o id do cliente logado;
- editar origem e destino de corrida nao finalizada com o id do cliente logado;
- manter a lista atual quando a criacao falha;
- listar apenas corridas disponiveis para motorista;
- aceitar corrida com o id do motorista logado;
- recarregar corridas quando o aceite falha;
- receber corrida via WebSocket;
- exibir toast de erro claro para falhas da API.

Rodar os smoke tests E2E com Cypress (com o `npm start` rodando em outro terminal):

```bash
npm run e2e:run
```

Os smoke tests cobrem o redirect de visitante sem sessao para o login, a renderizacao do formulario de email/senha e a entrada como cliente ate a tela de corridas. A API e stubada com `cy.intercept`, entao rodam sem backend de pe.

## Build

```bash
npm run build
```

Os arquivos de producao sao gerados em `dist/ride-challenge-frontend`.

## Docker

O frontend possui um `Dockerfile` multi-stage (build Node + nginx) e faz parte do `docker-compose.yml` do backend. Ao subir o compose, a aplicacao fica disponivel em `http://localhost:4200` servida por nginx, sem precisar de `npm start`.

Para buildar a imagem isoladamente:

```bash
docker build -t ride-frontend .
```

## Fluxo da aplicacao

1. Na tela de login, entre com email e senha ou crie uma conta com senha.
2. O frontend chama `POST /auth/login`, salva o JWT e envia `Authorization: Bearer <token>` nas chamadas REST.
3. Use tipo `CLIENT` para criar corridas.
4. Use tipo `DRIVER` para visualizar corridas disponiveis.
5. Cliente informa origem e destino.
6. Frontend chama `POST /rides` no backend.
7. Enquanto a corrida nao estiver finalizada, o cliente pode alterar origem/destino via `PUT /rides/{id}`.
8. Backend publica a corrida na fila Kafka.
9. Motoristas recebem notificacao em tempo real via WebSocket.
10. Motorista aceita a corrida.
11. Frontend chama `POST /rides/{id}/accept`.
12. Corrida fica vinculada ao motorista e muda para `IN_PROGRESS`.

## Telas principais

- Login e criacao de conta.
- Corridas do cliente.
- Corridas disponiveis para motorista.
- Tela de acesso negado quando o perfil nao tem permissao.

## Tratamento de erros

O frontend possui interceptor global para erros HTTP. Quando o backend retorna erro, a UI mostra um toast com mensagem clara para o usuario.

Exemplos:

- validacao de formulario;
- credenciais invalidas ou token expirado;
- recurso nao encontrado;
- corrida ja aceita por outro motorista;
- erro inesperado do servidor.

## Backend esperado

Antes de testar o frontend, suba o backend:

```bash
cd ../ride-challenge-backend
docker compose up --build
```

Com o backend de pe, o gateway deve responder em:

```text
http://localhost:8080
```
