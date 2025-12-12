&nbsp; Turmas:

&nbsp;  - GET /api/turmas - List all turmas

&nbsp;  - GET /api/turmas/{id} - View turma details

&nbsp;  - POST /api/turmas - Create turma

&nbsp;  - PUT /api/turmas/{id} - Update turma

&nbsp;  - DELETE /api/turmas/{id} - Delete turma

&nbsp;  - GET /api/turmas/escola/{escolaId} - List turmas by escola

&nbsp;  - GET /api/turmas/professor/{professorId} - List turmas by professor



&nbsp; Publicacoes:

&nbsp;  - GET /api/publicacoes - List all publicacoes

&nbsp;  - GET /api/publicacoes/{id} - View publicacao details (includes hashtags field with marcations like #bruno #felipe)

&nbsp;  - POST /api/publicacoes - Create publicacao (include hashtags field as array of strings for marcations)

&nbsp;  - PUT /api/publicacoes/{id} - Update publicacao (include hashtags field as array of strings for marcations)

&nbsp;  - DELETE /api/publicacoes/{id} - Delete publicacao

&nbsp;  - GET /api/publicacoes/turma/{turmaId} - List publicacoes by turma

&nbsp;  - GET /api/publicacoes/escola/{escolaId} - List publicacoes by escola

&nbsp;  - GET /api/publicacoes/public/{isPublic} - List publicacoes by visibility

&nbsp;  - GET /api/publicacoes/username/{username} - List publicacoes by username



&nbsp; Escolas:

&nbsp;  - GET /api/escolas - List all escolas

&nbsp;  - GET /api/escolas/{id} - View escola details

&nbsp;  - POST /api/escolas - Create escola

&nbsp;  - PUT /api/escolas/{id} - Update escola

&nbsp;  - DELETE /api/escolas/{id} - Delete escola

&nbsp;  - GET /api/escolas/ativo/{isActive} - List active/inactive escolas

&nbsp;  - GET /api/escolas/nome/{nome} - Search escolas by name

