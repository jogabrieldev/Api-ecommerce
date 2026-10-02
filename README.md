# E-commerce API

API REST de comércio eletrônico desenvolvida com Java e Spring Boot. O projeto implementa os principais processos de uma loja virtual, abrangendo gerenciamento de catálogo, clientes, carrinho de compras, checkout, pedidos, pagamentos simulados e acompanhamento financeiro.

O desenvolvimento utiliza uma organização inspirada em Clean Architecture, buscando separar regras de negócio, casos de uso, persistência e comunicação HTTP.

## Funcionalidades implementadas

### Administradores

- Cadastro e listagem de administradores;
- senhas protegidas com BCrypt;
- validação de e-mail e CPF únicos;
- papéis administrativos `ADMIN` e `MANAGER`;
- controle de administradores ativos;
- identificação do administrador responsável por cada produto;
- consulta de resumo financeiro individual.

### Clientes

- Cadastro de clientes e endereços;
- validação dos dígitos verificadores do CPF;
- normalização de e-mail, CPF e telefone;
- validação de data de nascimento;
- senhas armazenadas com BCrypt;
- autenticação e emissão de token JWT;
- controle de clientes ativos.

### Categorias e produtos

- Cadastro de categorias com nome único;
- cadastro de produtos vinculados a uma categoria;
- associação do produto ao administrador responsável;
- validação de preço e estoque;
- bloqueio de operações realizadas por administradores inativos;
- bloqueio de produtos em categorias inativas;
- listagem e consulta individual de produtos;
- pesquisa por nome e categoria;
- paginação dos resultados de pesquisa.

### Carrinho de compras

- Criação automática de carrinho ativo;
- adição de produtos;
- acumulação de quantidade para itens repetidos;
- alteração de quantidade;
- remoção de itens;
- limpeza do carrinho;
- consulta do carrinho ativo;
- validação da disponibilidade em estoque.

### Checkout, pedidos e pagamentos

- Finalização autenticada do carrinho;
- validação de propriedade do cliente;
- criação de pedido e itens de pedido;
- preservação do preço dos produtos no momento da compra;
- validação do preço atual antes de qualquer cobrança;
- atualização do preço aceito ao adicionar novamente ou alterar a quantidade do item;
- redução transacional do estoque;
- pagamentos simulados aprovados ou recusados;
- suporte a cartão de crédito, cartão de débito e PIX;
- armazenamento de motivo e referência de pagamentos recusados;
- idempotência para evitar o processamento duplicado do checkout;
- distribuição dos valores da venda entre os administradores responsáveis pelos produtos.

### Resumo financeiro

Cada administrador pode consultar informações relacionadas aos próprios produtos:

- valor total recebido em pagamentos aprovados;
- quantidade de unidades vendidas;
- estoque atual dos produtos cadastrados.

## Arquitetura

O projeto utiliza uma abordagem pragmática inspirada em Clean Architecture:

```text
presentation/rest
       |
       v
application/usecase
       |
       v
domain
       ^
       |
infrastructure
```

### Domain

Contém as entidades e regras centrais da aplicação, incluindo clientes, administradores, produtos, categorias, carrinhos, pedidos e pagamentos.

Também define:

- exceções de negócio;
- contratos de repositório;
- contrato do gateway de pagamento;
- abstração para proteção de senhas;
- validação personalizada de CPF.

As entidades possuem anotações JPA. Essa decisão mantém o projeto mais direto, evitando a duplicação entre modelos de domínio e persistência.

### Application

Contém os casos de uso responsáveis por coordenar os fluxos da aplicação.

Essa camada realiza operações como:

- cadastrar clientes e administradores;
- criar e pesquisar produtos;
- manipular o carrinho;
- executar o checkout;
- consultar informações financeiras.

Os casos de uso dependem dos contratos definidos no domínio, sem conhecer detalhes dos controllers ou do acesso ao banco.

### Infrastructure

Implementa os recursos técnicos utilizados pela aplicação:

- persistência com JPA, Hibernate, PostgreSQL e Flyway;
- consultas e agregações com JPQL;
- bloqueios pessimistas para operações concorrentes;
- hash de senhas com BCrypt;
- autenticação com Spring Security;
- geração e validação de JWT;
- gateway de pagamento simulado.

### Presentation

Expõe as funcionalidades por meio de uma API REST.

Essa camada contém:

- controllers;
- objetos de requisição e resposta;
- validação dos dados recebidos;
- conversão dos resultados para JSON;
- tratamento centralizado de exceções;
- definição dos códigos HTTP retornados.

## Segurança

A aplicação utiliza Spring Security com sessões stateless.

Clientes são autenticados por e-mail e senha e recebem um token JWT para operações protegidas. Administradores são carregados com seus papéis e permissões, permitindo controlar operações como cadastro de produtos e consulta financeira.

As senhas nunca são armazenadas em texto puro e são protegidas com BCrypt.

## Persistência e consistência

A aplicação utiliza PostgreSQL com JPA, Hibernate e migrations versionadas pelo Flyway.

Os fluxos que alteram múltiplos registros são transacionais. Operações de carrinho e checkout utilizam bloqueios no banco para reduzir conflitos entre requisições simultâneas.

Durante o checkout, os produtos são bloqueados e processados em ordem determinística. Isso protege o estoque e reduz o risco de inconsistências e deadlocks.

A chave de idempotência impede que a mesma tentativa de pagamento seja processada mais de uma vez.

## Pagamentos simulados

O projeto utiliza uma implementação interna de gateway de pagamento. Ela permite reproduzir pagamentos aprovados e recusados sem depender de serviços externos.

O resultado do pagamento é armazenado com status, valor, método, referência e, quando aplicável, motivo da recusa.

## Tratamento de erros

As exceções são tratadas de forma centralizada e convertidas em respostas HTTP padronizadas.

A aplicação diferencia situações como:

- dados de entrada inválidos;
- violação de regra de negócio;
- recurso não encontrado;
- operação não autorizada;
- conflito de dados;
- pagamento recusado;
- erro interno inesperado.

## Testes

O projeto possui testes automatizados para regras e fluxos importantes, incluindo:

- validação de CPF;
- cadastro de clientes;
- manipulação do carrinho;
- pesquisa e paginação de produtos;
- checkout aprovado e recusado;
- redução de estoque;
- idempotência de pagamentos;
- autorização do cliente;
- resumo financeiro;
- geração e validação de JWT;
- inicialização do contexto Spring.

Para executar os testes:

```bash
mvn test
```

## Tecnologias

- Java 21;
- Spring Boot 4;
- Spring Web MVC;
- Spring Security;
- Spring Data JPA;
- Hibernate;
- Jakarta Validation;
- PostgreSQL;
- Flyway;
- Maven;
- JUnit;
- Mockito;
- Lombok.

## Integração Fake Store API

A Fake Store API é utilizada exclusivamente como fonte inicial do catálogo. O endpoint
externo consumido é `GET https://fakestoreapi.com/products`; usuários, carrinhos e
autenticação externos não são importados.

Um administrador autenticado pode executar:

```http
POST /products/import/fake-store
Authorization: Basic <credenciais-administrativas>
```

O cliente HTTP possui timeouts configuráveis. Cada resposta é desserializada em um DTO
externo, validada, normalizada e convertida antes da persistência. As categorias conhecidas
são traduzidas (`electronics` para `Eletrônicos`, `jewelery` para `Joias`, e as categorias
de roupas masculinas e femininas). Títulos e descrições são preservados quando não existe
tradução local segura; a abstração `TranslationService` permite conectar outro tradutor no futuro.

Produtos são identificados pela combinação `source = FAKE_STORE` e `externalId`. Chamadas
subsequentes atualizam o produto já existente, evitando duplicidade. Após a importação,
carrinho, estoque, pedidos e pagamentos utilizam somente os produtos persistidos no PostgreSQL.

Exemplo de resposta:

```json
{
  "totalReceived": 20,
  "totalImported": 18,
  "totalUpdated": 2,
  "totalErrors": 0
}
```

Configurações disponíveis: `FAKE_STORE_BASE_URL`, `FAKE_STORE_CONNECT_TIMEOUT` e
`FAKE_STORE_READ_TIMEOUT`.

## Bootstrap do primeiro gerente

O bootstrap fica desativado por padrão e cria exclusivamente o primeiro usuário com papel
`MANAGER`. Ele não executa quando já existe qualquer administrador e nunca possui credenciais
padrão. Para uma instalação nova, configure temporariamente:

```text
BOOTSTRAP_INITIAL_MANAGER_ENABLED=true
BOOTSTRAP_INITIAL_MANAGER_NAME=Gerente Inicial
BOOTSTRAP_INITIAL_MANAGER_EMAIL=manager@example.com
BOOTSTRAP_INITIAL_MANAGER_PASSWORD=<senha forte entre 12 e 72 caracteres>
BOOTSTRAP_INITIAL_MANAGER_CPF=<CPF válido>
```

Depois da primeira inicialização bem-sucedida, remova as variáveis de bootstrap. A senha é
armazenada somente como hash BCrypt.

## Swagger

Com a aplicação ativa, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

No botão **Authorize**, use `bearerAuth` com o JWT retornado por
`POST /customers/authentication` para o checkout. Operações administrativas usam
`basicAuth`, com e-mail e senha de administrador.

## Executando a aplicação

É necessário ter Java 21 e PostgreSQL disponíveis.

Crie o banco utilizado pela aplicação:

```sql
CREATE DATABASE ecommerce_db;
```

Configure as variáveis de ambiente necessárias para o banco de dados e para a assinatura dos tokens JWT.

### Configuração e migrations

A aplicação utiliza uma configuração única. Por padrão, conecta ao PostgreSQL em `localhost`,
e os valores podem ser substituídos por `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
e `CORS_ALLOWED_ORIGINS`. O Hibernate usa `ddl-auto: validate`; toda alteração estrutural deve
ser criada em `db/migration`.

Para um banco vazio, o Flyway executa todas as migrations automaticamente. Para adotar Flyway
em um banco legado que já contenha as tabelas da aplicação, habilite explicitamente e de forma temporária
`SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` na primeira inicialização e remova a variável depois.
A migration de identidades interrompe a inicialização caso encontre e-mail ou CPF compartilhado
entre cliente e administrador; esses dados devem ser saneados antes de tentar novamente.

Execute a aplicação com o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

A API será iniciada, por padrão, em:

```text
http://localhost:8080
```
