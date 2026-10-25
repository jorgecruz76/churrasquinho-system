package br.com.churrasquinho.repository;

import br.com.churrasquinho.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByCliente_IdOrderByIdDesc(Long clienteId);
}