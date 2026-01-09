# Documentação do Backend (OBECI)

Este documento descreve os endpoints principais e os utilitários/funções adicionados para autenticação via JWT + Cookie, além do modo `dev`/`prod`.

## Perfis (DEV/PROD)

O projeto usa profiles do Spring Boot:

- `dev` (default)
- `prod`

Arquivos:

- `src/main/resources/application.yml` (base + `spring.profiles.default=dev`)
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

### Configurações (por propriedade)

**CORS** (`app.cors.*`)

- `app.cors.allowed-origins`: lista de origens permitidas
- `app.cors.allowed-methods`: métodos permitidos
- `app.cors.allowed-headers`: headers permitidos
- `app.cors.allow-credentials`: se envia cookies/credenciais

**Cookie do token** (`app.auth.cookie.*`)

- `name`: nome do cookie (default `token`)
- `http-only`: protege contra acesso via JS (recomendado `true`)
- `secure`: `true` em HTTPS (recomendado em produção)
- `same-site`: `Lax` (dev) / `None` (cenários cross-site) / `Strict`
- `path`: normalmente `/`
- `max-age-seconds`: tempo de vida do cookie
- `domain`: opcional (defina apenas quando necessário)

**JWT** (`app.jwt.*`)

- `secret`: segredo HS256 (mínimo 32 bytes). Em produção defina via env `APP_JWT_SECRET`.
- `expiration-seconds`: expiração do JWT (em segundos)
- `require-secret`: em `prod` deve ser `true` (API falha ao subir sem secret)

### Como rodar com profile

- Rodar em `dev` (default): só executar normalmente.
- Rodar em `prod`:
  - setar `SPRING_PROFILES_ACTIVE=prod`
  - setar `APP_JWT_SECRET` (obrigatório)

## Autenticação

- Login gera um JWT e envia de duas formas:
  - cookie HttpOnly (principal)
  - corpo da resposta `{ token, username }` (compatibilidade)

- O filtro `JwtRequestFilter` aceita:
  - `Authorization: Bearer <token>` (header)
  - cookie `token` (default)

- Se o token estiver inválido/expirado, o backend manda um `Set-Cookie` expirando o cookie.

## Endpoints

### Auth (`/auth`)

- `POST /auth/login`
  - Autentica email/senha
  - Retorna cookie `token` (HttpOnly) e também `{ token, username }`

- `POST /auth/logout`
  - Limpa o cookie `token`

- `GET /auth/me`
  - Retorna dados do usuário autenticado (username, email, roles)
  - Se não autenticado: retorna `401` e limpa cookie

- `PUT /auth/me`
  - Atualiza dados do próprio usuário
  - Requer autenticação
  - Se o usuário mudar o email, o JWT é reemitido e o cookie atualizado

- `POST /auth/register`
  - Cria usuário
  - **Requer role ADMIN**

### Usuários (`/api/usuarios`)

- `/api/usuarios/**`
  - **Somente ADMIN** (CRUD)

### Escolas (`/api/escolas`)

- `GET /api/escolas/**`: autenticado
- `POST/PUT/DELETE /api/escolas/**`: ADMIN

### Turmas (`/api/turmas`)

- `GET /api/turmas/**`: autenticado
- `POST/PUT/DELETE /api/turmas/**`: ADMIN

### Publicações (`/api/publicacoes`)

- `/api/publicacoes/**`: autenticado

## Funções/Classes (o que foi centralizado)

### `TokenCookieService`

Responsável por centralizar:

- `createAuthCookie(token)`: cria o cookie de autenticação com as flags corretas por ambiente
- `clearAuthCookie()`: expira o cookie (logout/invalidar)
- `getCookieName()`: nome configurável do cookie

### `AuthCookieProperties`

Properties para o cookie (`app.auth.cookie.*`).

### `JwtProperties`

Properties para JWT (`app.jwt.*`), incluindo `require-secret` para produção.

### `AppCorsProperties`

Properties de CORS (`app.cors.*`), removendo hardcode de origem.

### `JwtUtil`

- Usa `app.jwt.secret` + `expiration-seconds`
- Em `prod`, `require-secret=true` impede subir sem segredo

### `JwtRequestFilter`

- Lê token do header Bearer ou do cookie configurado
- Limpa cookie quando token é inválido/expirado
