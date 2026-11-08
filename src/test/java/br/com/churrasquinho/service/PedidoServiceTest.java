package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.ItemPedido;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private Produto produto;

    @BeforeEach
    void setUp() {
        // Prepara um produto com estoque 2
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Churrasquinho de Carne");
        produto.setEstoque(2);

        // Prepara um item com quantidade 5 (vai deixar o estoque negativo)
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(5);

        // Prepara um pedido PENDENTE
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setItens(List.of(item));
        item.setPedido(pedido);
    }

    @Test
    void deveConfirmarPedidoEDeixarEstoqueNegativo() {
        // Configura os mocks (simula o banco de dados)
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        // Executa a regra de negócio
        pedidoService.confirmarPedido(pedido);

        // Validações (O que esperamos que aconteça)
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus(), "O pedido deve estar CONFIRMADO");
        assertEquals(-3, produto.getEstoque(), "O estoque deve ser negativo (2 - 5 = -3)");
    }
}