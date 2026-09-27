# Adiministror — Frontend

Aplicação React + JavaScript integrada aos contratos do backend Spring Boot enviado. Coloque esta pasta `frontend` na raiz do projeto, ao lado do `pom.xml`. Nenhum arquivo do backend precisa ser substituído.

## Executar

Requer Node.js 22.12+ (ou Node 24) e npm. Em um terminal, mantenha o backend e o PostgreSQL em execução com suas configurações existentes. Em outro:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. Faça o cadastro de um DONO ou entre com uma conta existente. Não há usuário de demonstração nem bypass de login na aplicação. O ADMINISTRADOR deve existir no backend.

O proxy de desenvolvimento encaminha `/api` para `http://localhost:8083`, removendo o prefixo. Se seu backend usar outra porta, copie `.env.example` para `.env`, ajuste `BACKEND_URL` e reinicie o Vite. `VITE_API_BASE_URL` deve continuar `/api` quando usar o proxy. A porta 8083 é um padrão configurável, não uma exigência do código Java.

`npm run build` gera `dist`. `npm run preview` serve somente os arquivos gerados; para integração em produção, use um proxy reverso, como o Nginx incluído. Não coloque segredo JWT ou credenciais de banco em variáveis `VITE_*`.

## Telas e comportamento

- Login e cadastro sem escolha de role; JWT com `sub`, `email`, `role[]` e `exp`.
- DONO: dashboard financeiro e operacional, galerias próprias, salas, inquilinos, aluguéis, pagamentos, contratos, gastos, assistente e configurações.
- ADMINISTRADOR: visão administrativa, consulta de usuários, busca de galerias de outros donos e recursos liberados pelos Services. A API mantém a autorização definitiva.
- Aluguel em seis etapas: galeria, sala disponível, inquilino, condições, contrato e resumo. Uma requisição cria aluguel, contrato e primeiro pagamento.
- Pagamentos: filtros, histórico por aluguel, geração por competência, registro de recebimento e alteração do vencimento.
- Confirmação antes de excluir galeria, sala ou gasto e antes de encerrar aluguel. Histórico de aluguel e contratos consultável.
- Claro, escuro e sistema, com preferência persistida. Tutorial por usuário/perfil na primeira utilização e opção para refazê-lo.
- Listagens viram cards em telas pequenas. Modais usam `dialog`, com foco controlado pelo navegador. Respeito à preferência por movimento reduzido.
- Assistente exclusivamente local, identificado como prévia. Não envia mensagens a terceiros.

## Arquitetura

`src/services` concentra todas as chamadas HTTP. `api.js` configura o Axios, Bearer e erro 401. Consultas tratam 403 e falhas de rede; formulários exibem `validationErrors` junto aos campos. `src/adapters` normaliza os campos inconsistentes. `src/hooks/data.js` concentra consultas/mutações TanStack Query. Componentes não conhecem a URL do backend.

O JWT é decodificado para controlar a interface; sua assinatura e permissões são verificadas pelo servidor. Logout e 401 apagam token e cache. O token expira automaticamente pelo claim `exp`. O cache é limpo na troca de sessão para não reutilizar dados de outra conta. O armazenamento do token segue a sessão local do navegador; a configuração de segurança da hospedagem deve proteger contra XSS.

## Limitações confirmadas no backend

| Área                                              | Comportamento adotado                                                                                                                                     |
| ------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ContratoResponse sem ID                           | Renovação desabilitada. Service pronto, sem IDs inventados.                                                                                               |
| Sem listagem global de galerias                   | A lista inicial usa `/galeria/minhas`; admin pode buscar por nome. Contadores de galerias, salas e gastos no dashboard admin são explicitamente próprios. |
| UsuarioController retorna lista de ResponseEntity | Adapter aceita tanto `item.body` como o formato plano. Apenas leitura de usuários.                                                                        |
| GaleriaService.update ignora endereço             | Edição limitada a nome e telefone, preservando o endereço do response.                                                                                    |
| TenantService.update ignora documento             | Edição do documento e seu tipo desabilitada; nome, telefone e e-mail editáveis.                                                                           |
| AluguelResponse não inclui valorAluguel           | Edição exige informar explicitamente o valor desejado. Nunca infere valor a partir de pagamentos antigos.                                                 |
| SalasResponse retorna tenantId/tenantNome nulos   | Ocupação calculada pelos aluguéis ATIVOS, sem mutar responses.                                                                                            |
| AluguelResponse.dataVencimento é um dia textual   | Adapter converte para `diaVencimentoPadrao`.                                                                                                              |
| PagamentoResponse.aluguelID                       | Adapter converte para `aluguelId`. Status de atraso é o retornado pela API.                                                                               |
| Inquilinos não têm criação independente           | Cadastro/reaproveitamento ocorre no POST do aluguel. Listagem deduplicada por ID.                                                                         |
| Consulta de documento possui autorização          | Um 403 não é tratado como documento inexistente.                                                                                                          |
| Sem `/me`, edição de conta ou chat                | Configurações exibem e-mail/role do token. Somente chat tem mock de produção, centralizado.                                                               |

Comentários `TODO BACKEND` ficam junto aos services relevantes. Indicadores do dashboard usam todos os registros recebidos: receita por competência não deve ser interpretada como fluxo de caixa por data de recebimento. Gastos do dashboard somam todas as despesas das galerias próprias; a página Gastos permite recorte por período. Consultas agregadas por galeria podem demandar endpoints paginados/agregados conforme o volume crescer.

## Testes

```bash
npx playwright install chromium
npm test
```

Os testes interceptam a API com fixtures exclusivas de `tests/`, baseadas nos DTOs Java. Não substituem uma homologação com PostgreSQL e backend reais. Cobre as seis larguras solicitadas (375, 390, 430, 768, 1024 e 1440), DONO/ADMINISTRADOR, navegação, wizard, payloads de pagamento, guardas de acesso, 401/403, validação de formulário, tutorial e temas.

No ambiente Linux restrito de validação, `CI_CHROMIUM=1 npm test` usa Chromium empacotado em `@sparticuz/chromium`. No Windows use o comando padrão acima. As capturas em `docs/previews` usam exclusivamente dados de teste e não representam registros reais.

## Docker

O Dockerfile do frontend inclui build Node e Nginx com fallback para React Router e proxy `/api`. O Docker/Compose do backend foi preservado.

```bash
docker build -t adiministror-frontend ./frontend
docker run --rm -p 3000:80 -e BACKEND_UPSTREAM=http://host.docker.internal:8083 adiministror-frontend
```

O exemplo de `host.docker.internal` destina-se ao Docker Desktop. Em uma rede Compose compartilhada, use o nome real do serviço e sua porta interna, por exemplo `BACKEND_UPSTREAM=http://backend:8083`. O padrão do container é esse endereço; ajuste conforme seu Compose. Para hospedar, disponibilize frontend e proxy sob o mesmo domínio e configure HTTPS na infraestrutura.
