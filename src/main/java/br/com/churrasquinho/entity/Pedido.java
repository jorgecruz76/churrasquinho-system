package br.com.churrasquinho.entity;

import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.enums.TipoEntrega;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime dataPedido;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataSaida;

    private LocalDateTime dataEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    private Usuario entregador;

    @Column(length = 500)
    private String observacaoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", length = 20)
    private TipoEntrega tipoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @JsonManagedReference
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }

        if (dataPedido == null) {
            dataPedido = LocalDateTime.now();
        }

        if (status == null) {
            status = StatusPedido.PENDENTE;
        }

        if (valorTotal == null) {
            valorTotal = BigDecimal.ZERO;
        }
    }
}
