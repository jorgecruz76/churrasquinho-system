\# 📊 Dicionário de Dados - Churrasquinho System



Este documento descreve a estrutura física do banco de dados, contendo as tabelas, colunas, tipos de dados e restrições.



\## 1. Tabela: `usuarios`

Responsável pelo controle de acesso ao sistema.



| Coluna | Tipo | Restrição | Descrição |

|---|---|---|---|

| `id` | BIGINT | PK, Auto Increment | Identificador único do usuário. |

| `nome` | VARCHAR(100) | NOT NULL | Nome completo do usuário. |

| `email` | VARCHAR(100) | NOT NULL, UNIQUE | E-mail de login (não pode repetir). |

| `senha` | VARCHAR | NOT NULL | Senha criptografada (BCrypt). |

| `tipo` | VARCHAR(20) | NOT NULL | Perfil: ADM, CLIENTE ou ENTREGADOR. |

| `ativo` | BOOLEAN | NOT NULL | Indica se a conta está ativa. |



\## 2. Tabela: `clientes`

Dados comerciais e de entrega do cliente.



| Coluna | Tipo | Restrição | Descrição |

|---|---|---|---|

| `id` | BIGINT | PK, Auto Increment | Identificador único do cliente. |

| `nome` | VARCHAR(100) | NOT NULL | Nome do cliente. |

| `cpf` | VARCHAR(14) | UNIQUE | CPF (pode ser nulo em autocadastro). |

| `email` | VARCHAR(100) | UNIQUE | E-mail do cliente. |

| `telefone` | VARCHAR(20) | | Telefone de contato. |

| `logradouro` | VARCHAR(150) | | Rua/Avenida (pode ser nulo). |

| `numero` | VARCHAR(10) | | Número da residência. |

| `bairro` | VARCHAR(100) | | Bairro. |

| `cidade` | VARCHAR(100) | | Cidade. |

| `estado` | VARCHAR(2) | | UF (ex: SP, RS). |

| `cep` | VARCHAR(9) | | CEP. |

| `usuario\_id` | BIGINT | FK, UNIQUE | Vínculo com a tabela de usuários. |



\## 3. Tabela: `produtos`

Itens disponíveis para venda no catálogo.



| Coluna | Tipo | Restrição | Descrição |

|---|---|---|---|

| `id` | BIGINT | PK, Auto Increment | Identificador do produto. |

| `nome` | VARCHAR(100) | NOT NULL | Nome do produto (ex: Picanha). |

| `descricao` | VARCHAR(255) | | Detalhes do produto. |

| `preco` | DECIMAL(10,2) | NOT NULL | Preço unitário. |

| `estoque` | INT | NOT NULL | Quantidade em estoque (pode ficar negativo). |

| `ativo` | BOOLEAN | NOT NULL | Se está visível no catálogo. |

| `categoria\_id` | BIGINT | FK, NOT NULL | Vínculo com a categoria. |



\## 4. Tabela: `pedidos`

Registro das compras realizadas.



| Coluna | Tipo | Restrição | Descrição |

|---|---|---|---|

| `id` | BIGINT | PK, Auto Increment | Identificador do pedido. |

| `cliente\_id` | BIGINT | FK, NOT NULL | Quem fez o pedido. |

| `data\_pedido` | DATETIME | NOT NULL | Data e hora do pedido. |

| `status` | VARCHAR(30) | NOT NULL | Ex: PENDENTE, CONFIRMADO, ENTREGUE. |

| `valor\_total` | DECIMAL(10,2) | NOT NULL | Valor total calculado. |

| `tipo\_entrega` | VARCHAR(20) | | ENTREGA ou RETIRADA. |

| `entregador\_id` | BIGINT | FK | Quem realizou a entrega. |

| `observacao\_entrega`| VARCHAR(500) | | Notas do entregador. |



\## 5. Tabela: `itens\_pedido`

Detalhes dos produtos em cada pedido.



| Coluna | Tipo | Restrição | Descrição |

|---|---|---|---|

| `id` | BIGINT | PK, Auto Increment | Identificador do item. |

| `pedido\_id` | BIGINT | FK, NOT NULL | Vínculo com o pedido. |

| `produto\_id` | BIGINT | FK, NOT NULL | Vínculo com o produto. |

| `quantidade` | INT | NOT NULL | Quantidade comprada. |

| `preco\_unitario` | DECIMAL(10,2) | NOT NULL | Preço no momento da compra. |

| `subtotal` | DECIMAL(10,2) | NOT NULL | Preço x Quantidade. |



\*(Você pode adicionar as tabelas de pagamentos, entregas e categorias seguindo o mesmo padrão).\*

