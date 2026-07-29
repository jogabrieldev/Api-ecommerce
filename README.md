# E-commerce API

API REST de e-commerce desenvolvida como projeto de estudo com Java e Spring Boot.
O objetivo é construir gradualmente os principais processos de uma loja virtual,
praticando modelagem de domínio, regras de negócio, persistência, testes e princípios
de Clean Architecture.

Neste momento, o projeto não realiza integrações com gateways de pagamento,
transportadoras ou outros serviços externos. Essas funcionalidades serão inicialmente
representadas por regras e implementações internas.

## Funcionalidades atuais

- cadastro de administradores;
- cadastro de categorias;
- cadastro de produtos;
- associação obrigatória do produto a uma categoria;
- registro do administrador responsável pelo cadastro do produto;
- validação de e-mail e CPF únicos para administradores;
- validação de nome único para categorias;
- bloqueio do cadastro de produtos por administradores inativos;
- bloqueio do cadastro de produtos em categorias inativas;
- validação de preço e estoque.

### Cadastro de clientes

O fluxo de clientes inclui dados pessoais, senha protegida com BCrypt e endereÃ§o.
E-mail e CPF sÃ£o Ãºnicos, e o CPF Ã© validado pelos dÃ­gitos verificadores.

## Arquitetura

O projeto utiliza uma abordagem pragmática inspirada em Clean Architecture:

```text
presentation/rest
       |
       v
application/usecase
       |
       v
domain/repository
       ^
       |
infrastructure/persistence
```

### `domain`

Contém os modelos, exceções de negócio e contratos de persistência. Essa camada não
conhece controllers, códigos HTTP ou detalhes do acesso ao banco.

### `application`

Contém os casos de uso da aplicação, como criação de administradores, categorias e
produtos. Os casos de uso coordenam as regras do domínio e dependem somente das
interfaces de repositório.

### `infrastructure`

Contém os adaptadores responsáveis pela persistência com JPA e MySQL.

### `presentation`

Contém controllers REST, objetos de entrada e saída e o tratamento centralizado de
erros HTTP.

As classes de domínio ainda possuem anotações JPA. Essa é uma decisão pragmática para
evitar duplicação prematura entre modelos de domínio e modelos de persistência.

## Tecnologias

- Java 21;
- Spring Boot 4;
- Spring Web MVC;
- Spring Data JPA;
- Jakarta Validation;
- MySQL;
- Maven.

## Configuração

Crie o banco e o usuário configurados em `application.properties`:

```sql
CREATE DATABASE ecommerce_db;
```

Defina a senha do banco na variável de ambiente `DB_PASSWORD`.

No PowerShell:

```powershell
$env:DB_PASSWORD="sua-senha"
```

Execute a aplicação:

```powershell
mvn spring-boot:run
```

A API será iniciada em `http://localhost:8080`.

## Endpoints atuais

### Cadastrar administrador

```http
POST /administrators
Content-Type: application/json
```

```json
{
  "name": "Administrador",
  "email": "admin@email.com",
  "password": "uma-senha-segura",
  "cpf": "12345678901",
  "role": "ADMIN"
}
```

### Cadastrar categoria

```http
POST /categories
Content-Type: application/json
```

```json
{
  "name": "Eletrônicos",
  "description": "Produtos eletrônicos"
}
```

### Cadastrar produto

```http
POST /products
Content-Type: application/json
```

```json
{
  "name": "Notebook",
  "description": "Notebook para desenvolvimento",
  "price": 4500.00,
  "stock": 10,
  "administratorId": 1,
  "categoryId": 1
}
```

### Cadastrar cliente

```http
POST /customers
Content-Type: application/json
```

```json
{
  "name": "Maria da Silva",
  "email": "maria@email.com",
  "password": "uma-senha-segura",
  "cpf": "529.982.247-25",
  "phone": "(11) 99999-8888",
  "birthDate": "1990-05-20",
  "address": {
    "zipCode": "01310-100",
    "street": "Avenida Paulista",
    "number": "1000",
    "complement": "Apto 10",
    "neighborhood": "Bela Vista",
    "city": "SÃ£o Paulo",
    "state": "SP"
  }
}
```

O cliente e seu endereÃ§o sÃ£o persistidos na mesma transaÃ§Ã£o. Com
`spring.jpa.hibernate.ddl-auto=update`, as tabelas `customers` e
`customer_addresses` sÃ£o criadas automaticamente ao iniciar a aplicaÃ§Ã£o.

Os endpoints de cadastro retornam:

```json
{
  "id": 1,
  "name": "Nome do recurso"
}
```

## Próximas etapas

O projeto poderá evoluir com:

- autenticação e autorização;
- clientes e endereços;
- carrinho de compras;
- pedidos e itens de pedido;
- controle transacional de estoque;
- pagamentos e entregas simulados;
- migrations de banco;
- documentação OpenAPI;
- testes unitários e de integração.
