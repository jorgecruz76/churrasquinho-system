# 📐 Diagramas UML

## Diagrama de Classes

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String nome
        +String email
        +String senha
        +TipoUsuario tipo
        +Boolean ativo
    }

    class Cliente {
        +Long id
        +String nome
        +String cpf
        +String telefone
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
        +TipoEntrega tipoEntrega
        +Cliente cliente
        +Usuario entregador
    }

    class ItemPedido {
        +Long id
        +Integer quantidade
        +BigDecimal precoUnitario
        +BigDecimal subtotal
    }

    Usuario "1" -- "1" Cliente : possui perfil
    Cliente "1" -- "*" Pedido : realiza
    Pedido "1" *-- "*" ItemPedido : contém
    ItemPedido "*" -- "1" Produto : referencia