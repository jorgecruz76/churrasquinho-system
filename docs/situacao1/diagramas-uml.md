# Diagramas UML

## 1. Diagrama de Classes

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String nome
        +String email
        +String senha
        +TipoUsuario tipo
    }
    class Cliente {
        +Long id
        +String nome
        +String cpf
        +Usuario usuario
    }
    class Produto {
        +Long id
        +String nome
        +BigDecimal preco
        +Integer estoque
        +Categoria categoria
    }
    class Pedido {
        +Long id
        +LocalDateTime dataPedido
        +StatusPedido status
        +BigDecimal valorTotal
        +Cliente cliente
    }
    class ItemPedido {
        +Long id
        +Integer quantidade
        +BigDecimal subtotal
    }
    Usuario "1" -- "1" Cliente
    Cliente "1" -- "*" Pedido
    Pedido "1" *-- "*" ItemPedido
    ItemPedido "*" -- "1" Produto
    ```

    ## 2. Diagrama de Casos de Uso

```mermaid
flowchart LR
    Cliente([Cliente])
    ADM([ADM])
    Entregador([Entregador])

    UC1["Navegar no Catalogo"]
    UC2["Gerenciar Carrinho"]
    UC3["Finalizar Pedido"]
    UC4["Realizar Pagamento"]
    
    UC5["Gerenciar Usuarios"]
    UC6["Gerenciar Produtos"]
    
    UC7["Visualizar Entregas"]
    UC8["Confirmar Entrega"]

    Cliente --> UC1
    Cliente --> UC2
    Cliente --> UC3
    Cliente --> UC4

    ADM --> UC5
    ADM --> UC6

    Entregador --> UC7
    Entregador --> UC8