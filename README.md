# Armazém — API REST

API de controle de estoque construída com Java 21 e Spring Boot, desenvolvida como avaliação
da disciplina **Redes II** do IFBA.

O sistema é distribuído em três máquinas virtuais Debian, com o Nginx atuando como proxy
reverso e ponto único de entrada. Este repositório contém o **back-end** e os **arquivos de
infraestrutura** (scripts SQL, configuração do Nginx e unidade systemd).

---

## Sumário

- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Contrato da API](#contrato-da-api)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Decisões de projeto](#decisões-de-projeto)
- [Banco de dados](#banco-de-dados)
- [Segurança](#segurança)
- [Executando localmente](#executando-localmente)
- [Implantação nas VMs](#implantação-nas-vms)

---

## Arquitetura

```
   NAVEGADOR                VM1                   VM2                    VM3
                      192.168.122.11        192.168.122.12         192.168.122.13

                          Nginx               Spring Boot               MySQL
        │                   │                      │                      │
        │  GET /api/products│                      │                      │
        ├──────────────────►│                      │                      │
        │                   │ remove o prefixo /api│                      │
        │                   │  GET /products       │                      │
        │                   ├─────────────────────►│                      │
        │                   │                      │  SELECT * FROM ...   │
        │                   │                      ├─────────────────────►│
        │                   │                      │◄─────────────────────┤
        │◄──────────────────┤◄─────────────────────┤                      │
        │   200 [{...}]     │                      │                      │
```

| VM | Papel | Serviço |
|---|---|---|
| **VM1** | Ponto de entrada | Nginx — serve o front-end e encaminha `/api/` para a VM2 |
| **VM2** | Processamento | API REST Java (Spring Boot) na porta 8080 |
| **VM3** | Dados | MySQL 8.4 |

As três se comunicam por uma rede virtual interna (libvirt, faixa `192.168.122.0/24`).
Apenas a VM1 é acessível de fora — as VMs 2 e 3 nunca recebem conexão externa.

---

## Tecnologias

| | |
|---|---|
| **Java** | 21 |
| **Spring Boot** | 4.1.1 |
| **Persistência** | Spring Data JPA / Hibernate |
| **Banco de dados** | MySQL 8.4 |
| **Validação** | Jakarta Bean Validation |
| **Criptografia** | `spring-security-crypto` (BCrypt) |
| **Build** | Maven (via wrapper `./mvnw`) |

---

## Contrato da API

Base: `http://192.168.122.11/api`

| Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/auth/login` | `{login, password}` | `200 {token}` | `401` |
| `POST` | `/users` | `{name, login, password}` | `201` | `409` |
| `GET` | `/products` | — | `200 [{id,name,quantity,price}]` | |
| `GET` | `/products/{id}` | — | `200 {...}` | `404` |
| `GET` | `/products/lastId` | — | `200 {id}` | |
| `POST` | `/products` | `{name, quantity, price}` | `201` | `400` |
| `PUT` | `/products/{id}` | `{name, quantity, price}` | `200` | `404`, `400` |
| `PATCH` | `/products/{id}` | `{op, quantity}` | `200` | `404`, `400` |
| `DELETE` | `/products/{id}` | — | `204` | `404` |

Todas as rotas, **exceto as duas primeiras**, exigem o header:

```
Authorization: Bearer <token>
```

Sem um token válido, a resposta é `401`.

### Ajuste de estoque

O `PATCH` recebe uma operação e uma quantidade — não o valor final:

```json
{ "op": "add", "quantity": 10 }
{ "op": "remove", "quantity": 3 }
```

Em `remove`, o serviço verifica se há saldo suficiente antes de subtrair. Se não houver, a
operação é recusada com `400` e a mensagem informa o estoque disponível.

### Formato de erro

Toda resposta de erro segue o mesmo formato:

```json
{ "message": "Quantidade insuficiente em estoque! Estoque atual: 20" }
```

### Sobre `/products/lastId`

Retorna uma **estimativa** do próximo id (`MAX(id) + 1`), usada apenas para exibição na tela
de cadastro antes de salvar. O id definitivo é atribuído pelo `AUTO_INCREMENT` do banco no
momento da inserção — se houve exclusões, os valores divergem, e isso é esperado.

---

## Estrutura do projeto

```
src/main/java/com/example/armazem/
├── ArmazemApplication.java        classe principal; declara o bean PasswordEncoder
│
├── entity/                        mapeamento objeto-relacional
│   ├── Product.java
│   ├── User.java
│   └── Session.java               @ManyToOne com User (LAZY)
│
├── repository/                    acesso a dados (interfaces do Spring Data)
│   ├── ProductRepository.java     inclui @Query para o próximo id
│   ├── UserRepository.java        findByLogin / existsByLogin
│   └── SessionRepository.java     findByToken
│
├── dto/                           o JSON que entra e sai da API
│   ├── ProductRequest.java        ProductResponse.java
│   ├── QuantityPatchRequest.java  ErrorResponse.java
│   ├── CreateUserRequest.java     LoginRequest.java
│   └── LoginResponse.java
│
├── service/                       regras de negócio
│   ├── ProductService.java        CRUD + regra de estoque
│   ├── UserService.java           cadastro, hash BCrypt, login duplicado
│   └── AuthService.java           credenciais, geração e validação de token
│
├── controller/                    camada HTTP (fina, sem regra de negócio)
│   ├── ProductController.java
│   ├── UserController.java
│   └── AuthController.java
│
├── exception/                     erros de domínio + tradução para HTTP
│   ├── NotFoundException.java     → 404
│   ├── ConflictException.java     → 409
│   ├── BusinessException.java     → 400
│   ├── UnauthorizedException.java → 401
│   └── GlobalExceptionHandler.java
│
└── config/                        proteção das rotas
    ├── AuthInterceptor.java       valida o token antes do controller
    └── WebConfig.java             registra o interceptor e as exclusões

infra/
├── db/
│   ├── 01-schema.sql              cria o banco e as três tabelas
│   ├── 02-usuario.sql             usuário dedicado com privilégio mínimo
│   └── 03-seed.sql                usuário de demonstração (opcional)
├── nginx/armazem.conf             proxy reverso + arquivos estáticos
└── systemd/armazem-api.service    unidade de serviço da VM2
```

### Fluxo de uma requisição

```
AuthInterceptor  →  Controller  →  Service  →  Repository  →  MySQL
   valida o          traduz o       aplica a     executa a
    token             HTTP           regra        consulta
```

Exceções lançadas em qualquer ponto são capturadas pelo `GlobalExceptionHandler` e
convertidas em resposta HTTP — **nenhum controller precisa de `try/catch`**.

---

## Decisões de projeto

### Schema escrito à mão, com `ddl-auto=validate`

O banco é definido pelo `infra/db/01-schema.sql`. O Hibernate está configurado apenas para
**conferir**, na inicialização, se as entidades Java correspondem às tabelas existentes:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Se houver divergência, a aplicação **não sobe** e o log aponta a coluna. Com `update`, o
Hibernate criaria as tabelas por conta própria e o script `.sql` deixaria de refletir a
realidade.

### DTOs em vez de entidades nas rotas

As entidades nunca são expostas pela API, por três motivos:

1. **Vazamento de dados** — `User` contém `passwordHash`; devolvê-la enviaria o hash ao cliente
2. **Formatos que não são entidades** — `{op, quantity}` do PATCH não corresponde a tabela alguma
3. **Desacoplamento** — renomear uma coluna não quebra o contrato com o front-end

### Sessões em tabela separada

Os tokens ficam em `sessions`, e não como coluna em `users`. Assim um usuário pode ter várias
sessões simultâneas (celular e desktop, por exemplo), e cada uma expira independentemente.

### `BigDecimal` para valores monetários

Preços usam `BigDecimal`/`DECIMAL(10,2)`, nunca `double`. Ponto flutuante binário não
representa valores decimais exatamente, o que produz erros de centavos ao acumular.

---

## Banco de dados

Três tabelas, criadas por `infra/db/01-schema.sql`:

**`products`** — id, name, quantity, price
Com `CHECK (quantity >= 0)`, segunda linha de defesa da regra de estoque caso o banco seja
acessado fora da aplicação.

**`users`** — id, name, login, password_hash, created_at
Com `UNIQUE` em `login`, o que garante o `409` mesmo se dois cadastros chegarem simultaneamente.
O `password_hash` é `CHAR(60)` — o tamanho exato de um hash BCrypt.

**`sessions`** — id, user_id, token, created_at, expires_at
Chave estrangeira para `users` com `ON DELETE CASCADE`: excluir um usuário remove suas sessões.
O `token` é `UNIQUE`.

### Usuário dedicado

A aplicação **não** usa `root`. O `infra/db/02-usuario.sql` cria um usuário com o mínimo
necessário, restrito ao IP da VM2:

```sql
CREATE USER 'armazem'@'192.168.122.12' IDENTIFIED BY '...';
GRANT SELECT, INSERT, UPDATE, DELETE ON ArmazemRedes.* TO 'armazem'@'192.168.122.12';
```

Sem `DROP`, sem `CREATE`, sem acesso a outros bancos, e sem conexão a partir de outra máquina.
Se a aplicação for comprometida, o alcance do atacante fica limitado a manipular o estoque.

---

## Segurança

### Senhas

Armazenadas como **hash BCrypt**, nunca em texto. No login, o sistema calcula o hash do que foi
digitado e compara com o armazenado — a senha original nunca é recuperada.

O BCrypt gera um *salt* aleatório por senha, embutido nos 60 caracteres do resultado. Dois
usuários com a mesma senha ficam com hashes diferentes, o que inviabiliza ataques por tabelas
pré-computadas.

### Tokens

Gerados com `SecureRandom` (32 bytes, codificados em Base64 URL-safe → 44 caracteres). Cada
sessão expira em 8 horas.

O token trafega no header `Authorization`, e não na URL, porque a URL seria registrada em texto
puro no log de acesso do Nginx.

### Proteção contra enumeração de usuários

Login inexistente e senha incorreta produzem **a mesma resposta**: `401` com mensagem idêntica.
Diferenciá-las permitiria descobrir quais logins existem no sistema sem acertar nenhuma senha.

### Credenciais fora do repositório

A senha do banco é injetada por variável de ambiente:

```properties
spring.datasource.password=${DB_PASSWORD}
```

Os arquivos versionados em `infra/` usam o marcador `SENHA_AQUI`, substituído apenas no momento
da execução em cada VM.

---

## Executando localmente

**Pré-requisitos:** Java 21 (JDK) e MySQL 8.

**1. Criar o banco e as tabelas**

```bash
mysql -u root -p < infra/db/01-schema.sql
```

**2. Ajustar a conexão**

Editar `src/main/resources/application.properties` com o usuário do seu MySQL local, e exportar
a senha:

```bash
export DB_PASSWORD='sua_senha'
```

**3. Executar**

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

**4. Testar**

```bash
# cadastrar um usuário
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" \
  -d '{"name":"Ricardo","login":"ricardo","password":"senha123"}'

# obter o token
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"login":"ricardo","password":"senha123"}'

# usar uma rota protegida
curl http://localhost:8080/products -H "Authorization: Bearer <token>"
```

---

## Implantação nas VMs

### VM3 — banco de dados

```bash
sudo apt install -y mysql-server
mysql -u root -p < infra/db/01-schema.sql
mysql -u root -p < infra/db/02-usuario.sql   # substituir SENHA_AQUI antes
```

### VM2 — API

```bash
sudo apt install -y openjdk-21-jdk-headless
./mvnw package -DskipTests
sudo cp target/armazem-0.0.1-SNAPSHOT.jar /opt/armazem/
```

A configuração específica do ambiente fica em `/opt/armazem/application.properties`, **ao lado
do jar** e fora do repositório:

```properties
spring.datasource.url=jdbc:mysql://192.168.122.13:3306/ArmazemRedes?useSSL=false&serverTimezone=America/Bahia&allowPublicKeyRetrieval=true
spring.datasource.username=armazem
```

O Spring lê esse arquivo **e** o empacotado no jar, dando precedência ao externo — assim o mesmo
artefato roda em qualquer ambiente, e apenas a configuração muda.

Instalação do serviço:

```bash
sudo cp infra/systemd/armazem-api.service /etc/systemd/system/
sudo sed -i 's/SENHA_AQUI/<senha real>/' /etc/systemd/system/armazem-api.service
sudo chmod 600 /etc/systemd/system/armazem-api.service
sudo systemctl daemon-reload
sudo systemctl enable --now armazem-api
```

O `enable` garante que a API suba automaticamente no boot da VM.

```bash
sudo journalctl -u armazem-api -f    # acompanhar o log
```

### VM1 — Nginx

```bash
sudo apt install -y nginx
sudo cp infra/nginx/armazem.conf /etc/nginx/sites-available/armazem
sudo ln -s /etc/nginx/sites-available/armazem /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

> **Atenção à barra final do `proxy_pass`:**
> `proxy_pass http://192.168.122.12:8080/;`
> Com a barra, `/api/products` chega à API como `/products` — por isso os controllers mapeiam
> sem o prefixo `/api`. Sem ela, todos os endpoints retornam `404`.

---

## Autor

Desenvolvido por **Ricardo Bessa** para a disciplina de Redes II — IFBA.

O front-end (React) é mantido em repositório separado.
