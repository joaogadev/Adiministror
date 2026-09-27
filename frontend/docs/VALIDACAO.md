# Validação do frontend

## Resultado

- Build de produção aprovado com `npm run build`.
- 22 cenários distintos de Playwright aprovados ao longo da validação e das reexecuções direcionadas.
- Navegação de DONO e ADMINISTRADOR nas larguras 375, 390, 430, 768, 1024 e 1440 px: sem overflow horizontal e sem erros de execução nas verificações finais.
- Criação de aluguel: sala ocupada excluída da seleção, payload numérico correto e uma única requisição de criação.
- Registro de pagamento e alteração de vencimento: PATCHs e campos correspondentes aos DTOs.
- Contratos sem ID: renovação desabilitada; tentativa de acesso de DONO a usuários: bloqueada.
- Respostas 401/403, erros por campo, cadastro sem role e login com JWT validados.
- Onboarding de ambos os perfis, reinício do tutorial e persistência do tema validados.
- Menu móvel, capturas de desktop/mobile e modo escuro inspecionados.
- Nenhum `.ts`/`.tsx` no código da aplicação; nenhuma chamada Axios ou URL de backend nos componentes/páginas.
- Comparação byte a byte com o ZIP recebido: nenhum arquivo original do backend alterado.

## Limites da validação

Os testes de navegador interceptam as requisições com fixtures construídas a partir dos Controllers/DTOs analisados. O backend e o PostgreSQL do usuário não foram executados neste ambiente. É necessário homologar com as configurações reais do projeto, especialmente autenticação, conflitos de exclusão, autorização e persistência. O container do frontend foi preparado, mas não foi executado com Docker aqui. Chromium valida os tamanhos de tela; não houve execução em aparelhos iPhone/Safari físicos.

As capturas em `previews/` contêm dados fictícios de teste. A aplicação entregue usa API real para as funcionalidades existentes; os únicos mocks acessíveis na aplicação ficam no assistente, explicitamente identificado como prévia.
