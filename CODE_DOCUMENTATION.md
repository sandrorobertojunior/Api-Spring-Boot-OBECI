# Documentação do Back-end (OBECI)

Este documento descreve, de forma objetiva, como o back-end funciona (arquitetura, autenticação, endpoints, regras e persistência). O foco é explicar o comportamento real observado no código, sem alterar lógica.

## 1) Visão geral

O back-end é uma API Spring Boot com:

- Autenticação baseada em JWT, priorizando cookie HttpOnly.
- Spring Security em modo stateless.
- Persistência via JPA/Hibernate.
- Estrutura em camadas: controllers (HTTP) → services (regras) → repositories (banco) → entities (modelo).
- Tratamento centralizado de exceções com respostas HTTP padronizadas.

## 2) Organização por camadas

### Controllers (camada HTTP)

Responsáveis por:

- Declarar rotas e contratos de entrada/saída (JSON).
- Validar entradas via Bean Validation (anotações como NotBlank, etc.).
- Delegar as regras para services.

### Services (regras de negócio)

Responsáveis por:

- Aplicar validações de domínio e regras (ex.: conflitos, permissões, consistência de dados).
- Orquestrar operações (salvar, atualizar, buscar, deletar).
- Normalizar dados de entrada quando necessário (ex.: lembretes, índices, etc.).

### Repositories (acesso ao banco)

Responsáveis por:

- Consultas e persistência via Spring Data JPA.
- Consultas derivadas e/ou queries específicas.

### Entities (modelo persistido)

Responsáveis por:

- Representar as tabelas/relacionamentos.
- Definir mapeamentos (ex.: relacionamentos entre escola/turma/usuário, imagens, etc.).

### DTOs (contratos de API)

Responsáveis por:

- Definir o que entra e sai nas rotas (sem expor detalhes internos desnecessários).
- Regras de validação de payload.

## 3) Autenticação e segurança

### Como o login funciona

- O endpoint de login autentica credenciais.
- Em caso de sucesso, gera um JWT e devolve:
  - O token no corpo (em algumas respostas), e
  - Um header Set-Cookie com cookie HttpOnly contendo o JWT.

### Como a autenticação é lida nas requisições

Um filtro de requisição tenta obter o token:

- Primeiro por header Authorization no formato Bearer.
- Se não houver, tenta por cookie.

Se o token for válido, o SecurityContext é preenchido para a requisição.
Se o token estiver inválido/expirado, o back-end pode emitir Set-Cookie para limpar o cookie.

### Regras de autorização (alto nível)

- Rotas públicas: login e logout.
- Rotas de administração: endpoints de gestão de usuários exigem role ADMIN.
- Rotas do próprio usuário:
  - Consulta de dados pode ter comportamento permissivo em alguns casos (ex.: rota que retorna 401 quando não autenticado, mesmo se acessível).
  - Operações de alteração exigem autenticação.

Observação importante:

- Há tratamento de exceções amplo para RuntimeException retornando 400, o que pode mascarar falhas internas se algo inesperado ocorrer.

## 4) Endpoints principais

Esta seção lista os endpoints e o comportamento esperado. Os nomes exatos das rotas seguem o padrão visto na aplicação.

### Autenticação

- POST /auth/register
  - Cria um usuário.

- POST /auth/login
  - Autentica e emite JWT.
  - Efeito colateral: envia Set-Cookie com o cookie de autenticação.

- POST /auth/logout
  - Expira o cookie de autenticação.
  - Efeito colateral: envia Set-Cookie com expiração.

- GET /auth/me
  - Retorna dados do usuário autenticado quando houver autenticação válida.
  - Se não autenticado, tende a responder 401.

- PUT /auth/me
  - Atualiza dados do usuário autenticado.
  - Se o email mudar, a aplicação tende a reemitir JWT para refletir a nova identidade.

### Lembretes do usuário autenticado

Persistência:

- Os lembretes ficam armazenados no próprio usuário, como uma lista de textos.
- O armazenamento usa um tipo compatível com PostgreSQL (array de texto).

Normalização de texto:

- O texto é normalizado para manter quebras de linha consistentes (conversão de CRLF para LF) e removendo espaços em excesso nas extremidades.

Endpoints:

- GET /auth/me/lembretes
  - Retorna a lista de lembretes.

- POST /auth/me/lembretes
  - Adiciona um lembrete.
  - Retorna a lista atualizada.

- PUT /auth/me/lembretes/{index}
  - Atualiza um lembrete por índice.
  - Retorna a lista atualizada.

- DELETE /auth/me/lembretes/{index}
  - Remove um lembrete por índice.
  - Retorna a lista atualizada.

Validações e erros comuns:

- Texto vazio ou em branco: 400.
- Índice fora do intervalo: 400.
- Não autenticado: 401.

### Usuários (admin)

- CRUD sob /api/usuarios
  - Restrito a role ADMIN.

- GET /api/usuarios/professores
  - Retorna uma lista de professores em um formato resumido.

### Escolas

- CRUD sob um prefixo /api/escolas.

### Turmas

- CRUD sob um prefixo /api/turmas.

Regra de conflito importante:

- Nome de turma deve ser único dentro da mesma escola.
- Quando violado, a aplicação retorna conflito 409.

### Instrumentos e imagens

- Há endpoints para criar/editar/listar instrumentos.
- Imagens são tratadas como entidade/relacionamento separado, com persistência associada.

## 5) Tratamento de erros e respostas

O back-end possui um handler global para padronizar erros.

Casos principais:

- Erros de validação (Bean Validation): 400 com lista de campos e mensagens.
- Conflito de turma duplicada (regra de negócio): 409.
- RuntimeException (genérico): 400.

Observação:

- Tratar RuntimeException como 400 é prático, mas amplo. Se algo realmente inesperado ocorrer, pode ser retornado como erro de cliente, mesmo sendo falha interna.

## 6) Configuração por ambiente (YAML)

A aplicação usa perfis para separar configurações:

- Perfil de desenvolvimento: tipicamente aponta para PostgreSQL local.
- Perfil de produção: espera variáveis de ambiente (URL, usuário, senha, secret de JWT, flags do cookie).
- Perfil de testes: costuma usar banco em memória.

Chaves importantes (alto nível):

- Configuração do JWT: secret, expiração e comportamento quando secret estiver ausente.
- Configuração do cookie de autenticação: nome, domínio/path, secure, sameSite e maxAge.
- Configuração de CORS: origens permitidas e se credenciais (cookies) são aceitas.

#### `TokenCookieService`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/TokenCookieService.java`
- **Propósito**: centralizar criação/limpeza do cookie JWT.
- **Classe principal**: `TokenCookieService`
  - `createAuthCookie(String token) -> ResponseCookie`: cookie com JWT.
  - `clearAuthCookie() -> ResponseCookie`: cookie expirado (Max-Age=0).
  - `getCookieName() -> String`: nome configurado.
- **Efeitos colaterais**: nenhum direto; o caller usa o header `Set-Cookie`.

#### `JwtRequestFilter`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/JwtRequestFilter.java`
- **Propósito**: autenticar cada request via JWT.
- **Classe principal**: `JwtRequestFilter` (`OncePerRequestFilter`)
  - `doFilterInternal(...)`: extrai token (Bearer ou cookie), valida e seta `SecurityContext`.
- **Efeitos colaterais**:
  - Define autenticação na thread da request.
  - Em token inválido/expirado, adiciona header `Set-Cookie` para limpar o cookie.
- **Dependências**: `UserDetailsService`, `JwtUtil`, `TokenCookieService`.

#### `AppCorsProperties`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/AppCorsProperties.java`
- **Propósito**: bind de `app.cors.*`.
- **Classe principal**: `AppCorsProperties`
- **Dependências**: `SecurityConfiguration.corsConfigurationSource()`.

#### `SecurityConfiguration`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/SecurityConfiguration.java`
- **Propósito**: configurar o Spring Security (stateless, CORS, rotas e filtro JWT).
- **Classe principal**: `SecurityConfiguration`
  - `passwordEncoder() -> PasswordEncoder`: BCrypt.
  - `filterChain(HttpSecurity) -> SecurityFilterChain`: regras por rota.
  - `authenticationManager(AuthenticationConfiguration) -> AuthenticationManager`.
  - `corsConfigurationSource() -> CorsConfigurationSource`.
- **Pontos críticos**:
  - `/auth/login` e `/auth/logout` são `permitAll`.
  - `/auth/me` GET é `permitAll`, PUT exige autenticação.
  - `/auth/me/lembretes/**` exige autenticação.
  - `/api/usuarios/**` exige role ADMIN.

#### `GlobalExceptionHandler`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/GlobalExceptionHandler.java`
- **Propósito**: padronizar erros REST.
- **Classe principal**: `GlobalExceptionHandler`
  - `handleDuplicateTurma(DuplicateTurmaException) -> 409`.
  - `handleValidation(MethodArgumentNotValidException) -> 400` com lista de erros.
  - `handleRuntime(RuntimeException) -> 400`.
- **Observação**: `RuntimeException` é tratado como 400; isso é amplo e pode mascarar erros internos.

#### `AdminBootstrap`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/AdminBootstrap.java`
- **Propósito**: garantir um usuário ADMIN padrão na inicialização (exceto profile `test`).
- **Classe principal**: `AdminBootstrap` (`CommandLineRunner`)
  - `run(String... args)`: cria admin se não existir.
- **Efeitos colaterais**: insere usuário no banco.

#### `CustomAuthFilter`
- **Arquivo**: `src/main/java/org/obeci/platform/configs/CustomAuthFilter.java`
- **Propósito**: filtro alternativo via Basic Auth.
- **Status no código**: não está registrado como bean/na cadeia de filtros; portanto pode estar inativo.
- **Efeitos colaterais**: se registrado, define autenticação no `SecurityContext`.

### 3) Controllers (`org.obeci.platform.controllers`)

#### `AuthController`
- **Arquivo**: `src/main/java/org/obeci/platform/controllers/AuthController.java`
- **Propósito**: autenticação, self-service e lembretes.
- **Classe principal**: `AuthController`
  - `POST /auth/register (UsuarioCreateRequest) -> Usuario`: cria usuário.
  - `POST /auth/login (AuthLoginRequest) -> {token,username}`: autentica e seta cookie.
  - `POST /auth/logout -> String`: expira cookie.
  - `GET /auth/me -> {username,email,arrayRoles}`: dados do usuário.
  - `PUT /auth/me (UsuarioSelfUpdateRequest) -> {username,email,arrayRoles}`: atualiza; reemite JWT se email muda.
  - `GET/POST/PUT/DELETE /auth/me/lembretes`: CRUD por índice (0..n-1).
- **Efeitos colaterais**:
  - `Set-Cookie` em login/logout e em cenários de não autenticado.
  - Writes no banco em register/update/lembretes.

#### `UsuarioController`
- **Arquivo**: `src/main/java/org/obeci/platform/controllers/UsuarioController.java`
- **Propósito**: endpoints administrativos de usuários.
- **Classe principal**: `UsuarioController`
  - CRUD sob `/api/usuarios`.
  - `GET /api/usuarios/professores -> List<ProfessorResponse>`.

#### `EscolaController`
- **Arquivo**: `src/main/java/org/obeci/platform/controllers/EscolaController.java`
- **Propósito**: CRUD e consultas para escolas.
- **Observação**: `EscolaCreateRequest.cidade` existe e é validado, mas não é persistido na entidade.

#### `TurmaController`
- **Arquivo**: `src/main/java/org/obeci/platform/controllers/TurmaController.java`
- **Propósito**: CRUD e consultas para turmas.
- **Ponto crítico**: `GET /api/turmas/mine` aplica regra baseada em roles (`ADMIN`/`PROFESSOR`).

#### `InstrumentoController`
- **Arquivo**: `src/main/java/org/obeci/platform/controllers/InstrumentoController.java`
- **Propósito**: slides JSON por turma e imagens.
- **Ponto crítico**: `GET /api/instrumentos/turma/{turmaId}` cria instrumento default se não existir.

### 4) Services (`org.obeci.platform.services`)

#### `UsuarioService`
- **Arquivo**: `src/main/java/org/obeci/platform/services/UsuarioService.java`
- **Propósito**: regras de usuários e integração com Spring Security.
- **Classe principal**: `UsuarioService`
  - `register(Usuario|UsuarioCreateRequest) -> Usuario`: valida unicidade e gera hash.
  - `login(email, senha) -> Optional<Usuario>`: valida credenciais.
  - `loadUserByUsername(username) -> UserDetails`: para Spring Security.
  - `update(id, changes) -> Optional<Usuario>`: aplica mudanças não vazias.
  - `findByRole(role, q) -> List<Usuario>`: filtra em memória após `findAll`.
  - `list/add/update/deleteLembrete(email, ...) -> List<String>`: CRUD de lembretes.
- **Pontos críticos**:
  - Vários erros são lançados como `RuntimeException` (tratados como 400).
  - `findByRole` faz filtro em memória (pode impactar performance conforme volume).

#### `TurmaService`
- **Arquivo**: `src/main/java/org/obeci/platform/services/TurmaService.java`
- **Propósito**: regras de turmas.
- **Ponto crítico**: regra de unicidade `nome` por `escolaId` (case-insensitive).
- **Efeito colateral**: tenta criar `Instrumento` default ao criar turma.

#### `EscolaService`
- **Arquivo**: `src/main/java/org/obeci/platform/services/EscolaService.java`
- **Propósito**: operações básicas de persistência para escolas.

#### `InstrumentoService`
- **Arquivo**: `src/main/java/org/obeci/platform/services/InstrumentoService.java`
- **Propósito**: persistir JSON e imagens.
- **Ponto crítico**: `defaultSlidesJson()` define o contrato do JSON inicial do editor.

### 5) Entities (`org.obeci.platform.entities`)

- `Usuario`: tabela `usuarios`, roles e lembretes como `text[]`.
- `Turma`: tabela `turmas`, ids de escola/professor como primitivos.
- `Escola`: tabela `escolas`.
- `Instrumento`: tabela `instrumentos` com `turma_id` único e `slides_json` TEXT.
- `InstrumentoImage`: tabela `instrumento_images` com LOB binário.

### 6) Repositories (`org.obeci.platform.repositories`)

- `UsuarioRepository`: queries por email/cpf e queries nativas para arrays (`ANY`, `&&`).
- `TurmaRepository`: queries derivadas e checks de duplicidade.
- `EscolaRepository`: filtros simples.
- `InstrumentoRepository`: lookup por `turmaId`.
- `InstrumentoImageRepository`: CRUD básico.

### 7) DTOs (`org.obeci.platform.dtos`)

- Requests:
  - `AuthLoginRequest`, `UsuarioCreateRequest`, `UsuarioUpdateRequest`, `UsuarioSelfUpdateRequest`
  - `EscolaCreateRequest`, `EscolaUpdateRequest`
  - `TurmaCreateRequest`, `TurmaUpdateRequest`
  - `LembreteRequest`
- Responses:
  - `ProfessorResponse`, `InstrumentoDto`

## Configuração (resources)

### `src/main/resources/application.yml`
- Base da aplicação; define profile default `dev` e configurações JPA/Hibernate + porta.

### `src/main/resources/application-dev.yml`
- DEV local: PostgreSQL local e defaults de CORS/cookie/JWT.

### `src/main/resources/application-prod.yml`
- PROD: datasource via env vars e políticas mais restritas (cookie secure, same-site None).

### `src/main/resources/application-test.yml`
- TEST: H2 em memória, ddl-auto none e desativa admin bootstrap.
