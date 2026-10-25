package br.com.churrasquinho.entity;

import br.com.churrasquinho.enums.StatusEntrega;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEntrega status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataSaida;

    private LocalDateTime dataEntrega;

    @PrePersist
    public void prePersist() {

        if (status == null) {
            status = StatusEntrega.PENDENTE;
        }

        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}