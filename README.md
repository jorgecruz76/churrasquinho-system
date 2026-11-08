\#  Churrasquinho System



Sistema completo de vendas e entregas de churrasquinhos — projeto acadêmico.



\##  Sobre o Projeto



Sistema web para gerenciar todo o fluxo de venda de churrasquinho: do catálogo de produtos até a entrega ao cliente, com diferentes níveis de acesso (ADM, Cliente e Entregador).



\##  Funcionalidades



\###  Cliente

\-  Navegar pelo catálogo de produtos

\-  Buscar produtos por nome ou categoria

\-  Adicionar ao carrinho (com controle de quantidade)

\-  Finalizar pedido (entrega ou retirada)

\-  Realizar pagamento (PIX, cartão ou dinheiro)

\-  Acompanhar status do pedido (timeline visual)

\-  Consultar histórico de pedidos



\###  Administrador (ADM)

\-  Gerenciar usuários (ADM, Cliente, Entregador)

\-  Gerenciar categorias

\-  Gerenciar produtos (com destaque visual de estoque baixo/negativo)

\-  Gerenciar pedidos e status

\-  Gerenciar pagamentos

\-  Gerenciar entregas

\-  Imprimir pedidos (individual ou em lista)

\-  Visualizar dashboard em `/admin`



\###  Entregador

\-  Ver pedidos prontos para entrega

\-  Marcar pedidos como "saiu para entrega"

\-  Confirmar entrega com observações

\-  Histórico de entregas



\##  Tecnologias



| Camada | Tecnologia |

|---|---|

| Backend | Spring Boot 4.x |

| Persistência | Spring Data JPA + MySQL |

| Frontend | Thymeleaf + HTML/CSS/JS |

| Segurança | BCrypt (Spring Security Crypto) |

| Build | Maven |

| Auxiliares | Lombok |



\##  Como Rodar



\### Pré-requisitos

\- Java 21+

\- Maven 3.9+

\- MySQL 8+



\### Configuração do Banco



```sql

CREATE DATABASE churrasquinho\_db;

