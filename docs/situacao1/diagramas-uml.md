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
        +Boolean ativo
    }

    class Cliente {
        +Long id
        +String nome
        +String cpf
        +String telefone
        +String logradouro
        +String numero
        +String bairro
        +String cidade
        +String estado
        +String cep
        +Boolean ativo
    }

    class Categoria {
        +Long id
        +String nome
        +String descricao
        +Boolean ativo
    }

    class Produto {
        +Long id
        +String nome
        +String descricao
        +BigDecimal preco
        +Integer estoque
        +Boolean ativo
    }

    class Pedido {
        +Long id
        +LocalDateTime dataPedido
        +LocalDateTime dataCriacao
        +StatusPedido status
        +BigDecimal valorTotal
        +TipoEntrega tipoEntrega
        +String observacaoEntrega
    }

    class ItemPedido {
        +Long id
        +Integer quantidade
        +BigDecimal precoUnitario
        +BigDecimal subtotal
    }

    class Pagamento {
        +Long id
        +FormaPagamento formaPagamento
        +BigDecimal valor
        +StatusPagamento status
        +LocalDateTime dataPagamento
    }

    class Entrega {
        +Long id
        +StatusEntrega status
        +LocalDateTime dataCriacao
        +LocalDateTime dataSaida
        +LocalDateTime dataEntrega
    }

    %% Relacionamentos
    Usuario "1" -- "1" Cliente : possui perfil
    Usuario "1" -- "*" Pedido : atua como Entregador
    Cliente "1" -- "*" Pedido : realiza
    
    Categoria "1" -- "*" Produto : classifica
    Produto "1" -- "*" ItemPedido : está contido
    
    Pedido "1" *-- "*" ItemPedido : contém
    Pedido "1" -- "1" Pagamento : possui
    Pedido "1" -- "1" Entrega : possui```

    ---

## 2. Diagrama de Casos de Uso

```mermaid
flowchart LR
    %% Atores
    Cliente([Cliente])
    ADM([ADM])
    Entregador([Entregador])

    %% Casos de Uso
    UC1(Navegar no Catálogo)
    UC2(Gerenciar Carrinho)
    UC3(Finalizar Pedido)
    UC4(Realizar Pagamento)
    UC5(Acompanhar Pedido)
    
    UC6(Gerenciar Usuários)
    UC7(Gerenciar Produtos e Categorias)
    UC8(Gerenciar Pedidos e Pagamentos)
    UC9(Imprimir Pedidos)
    
    UC10(Visualizar Entregas Pendentes)
    UC11(Marcar Saiu para Entrega)
    UC12(Confirmar Entrega)

    %% Conexões do Cliente
    Cliente --> UC1
    Cliente --> UC2
    Cliente --> UC3
    Cliente --> UC4
    Cliente --> UC5

    %% Conexões do ADM
    ADM --> UC6
    ADM --> UC7
    ADM --> UC8
    ADM --> UC9

    %% Conexões do Entregador
    Entregador --> UC10
    Entregador --> UC11
    Entregador --> UC12