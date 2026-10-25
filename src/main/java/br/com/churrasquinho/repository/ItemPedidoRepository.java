package br.com.churrasquinho.repository;

import br.com.churrasquinho.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.churrasquinho.repository.ItemPedidoRepository;

import java.util.List;



public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedidoId(Long pedidoId);

    boolean existsByProdutoId(Long produtoId);
}