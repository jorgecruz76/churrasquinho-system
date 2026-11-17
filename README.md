# Churrasquinho System

**Vídeo de Apresentação do Projeto:** 

# Link Github
[Acessar aqui](https://github.com/jorgecruz76/churrasquinho-system)

Sistema completo de vendas e entregas de churrasquinhos — projeto acadêmico.

## Sobre o Projeto

Sistema web para gerenciar todo o fluxo de venda de churrasquinho: do catálogo de produtos até a entrega ao cliente, com diferentes níveis de acesso (ADM, Cliente e Entregador).

## Funcionalidades

### Cliente
- Navegar pelo catálogo de produtos
- Buscar produtos por nome ou categoria
- Adicionar ao carrinho (com controle de quantidade)
- Finalizar pedido (entrega ou retirada)
- Realizar pagamento (PIX, cartão ou dinheiro)
- Acompanhar status do pedido (timeline visual)
- Consultar histórico de pedidos

### Administrador (ADM)
- Gerenciar usuários (ADM, Cliente, Entregador)
- Gerenciar categorias
- Gerenciar produtos (com destaque visual de estoque baixo/negativo)
- Gerenciar pedidos e status
- Gerenciar pagamentos
- Gerenciar entregas
- Imprimir pedidos (individual ou em lista)
- Visualizar dashboard em `/admin`

### Entregador
- Ver pedidos prontos para entrega
- Marcar pedidos como "saiu para entrega"
- Confirmar entrega com observações
- Histórico de entregas

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Spring Boot 4.x |
| Persistência | Spring Data JPA + MySQL |
| Frontend | Thymeleaf + HTML/CSS/JS |
| Segurança | BCrypt (Spring Security Crypto) |
| Build | Maven |
| Auxiliares | Lombok |

## Como Rodar

### Pré-requisitos
- Java 21+
- Maven 3.9+
- MySQL 8+

### Configuração do Banco de Dados

1. Crie o banco no MySQL:

```sql
CREATE DATABASE churrasquinho_db;

-- Tabela de Usuários (Login)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL
);

-- Tabela de Clientes
CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    logradouro VARCHAR(150),
    numero VARCHAR(10),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(9),
    data_cadastro DATETIME NOT NULL,
    ativo BOOLEAN NOT NULL,
    usuario_id BIGINT UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabela de Categorias
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL
);

-- Tabela de Produtos
CREATE TABLE produtos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    preco DECIMAL(10,2) NOT NULL,
    estoque INT NOT NULL,
    ativo BOOLEAN NOT NULL,
    imagem_url VARCHAR(500),
    categoria_id BIGINT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

-- Tabela de Pedidos
CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    data_pedido DATETIME NOT NULL,
    data_criacao DATETIME NOT NULL,
    data_saida DATETIME,
    data_entrega DATETIME,
    status VARCHAR(30) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    tipo_entrega VARCHAR(20),
    entregador_id BIGINT,
    observacao_entrega VARCHAR(500),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (entregador_id) REFERENCES usuarios(id)
);

-- Tabela de Itens do Pedido
CREATE TABLE itens_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);

-- Tabela de Pagamentos
CREATE TABLE pagamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    forma_pagamento VARCHAR(30) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_pagamento DATETIME NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

-- Tabela de Entregas
CREATE TABLE entregas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    data_criacao DATETIME NOT NULL,
    data_saida DATETIME,
    data_entrega DATETIME,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run

O sistema exige um ADM para gerenciar os produtos, você pode criar um via banco de dados:

Cadastre um usuário normalmente pela tela de cadastro.

Altere o tipo dele no MySQL:

UPDATE usuarios SET tipo = 'ADM' WHERE email = 'seu@email.com';

Para rodar testes

.\mvnw.cmd test