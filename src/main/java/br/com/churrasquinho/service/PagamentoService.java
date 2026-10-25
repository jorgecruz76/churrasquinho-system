package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Pagamento;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.enums.StatusPagamento;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.repository.PagamentoRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;      // ← NOVO

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            PedidoRepository pedidoRepository,
            PedidoService pedidoService) {           // ← NOVO
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;           // ← NOVO
    }

    public Pagamento salvar(Pagamento pagamento) {

        if (pagamento.getPedido() == null ||
                pagamento.getPedido().getId() == null) {
            throw new IllegalArgumentException("O pedido é obrigatório.");
        }

        if (pagamento.getFormaPagamento() == null) {
            throw new IllegalArgumentException(
                    "A forma de pagamento é obrigatória.");
        }

        Pedido pedido = pedidoRepository
                .findById(pagamento.getPedido().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "Não é possível criar um pagamento para um pedido cancelado.");
        }

        if (pedido.getStatus() == StatusPedido.ENTREGUE ||
            pedido.getStatus() == StatusPedido.SAIU_PARA_ENTREGA) {
            throw new IllegalArgumentException(
                    "Não é possível criar pagamento para pedido já entregue ou em rota.");
        }

        if (pagamento.getId() == null &&
                pagamentoRepository.findAll().stream()
                        .anyMatch(p -> p.getPedido().getId().equals(pedido.getId()))) {
            throw new IllegalArgumentException(
                    "O pedido já possui um pagamento.");
        }

        pagamento.setPedido(pedido);

        BigDecimal valorPedido = pedido.getValorTotal();

        if (valorPedido == null || valorPedido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O pedido deve possuir um valor total maior que zero.");
        }

        pagamento.setValor(valorPedido);

        if (pagamento.getStatus() == null) {
            pagamento.setStatus(StatusPagamento.PENDENTE);
        }

        return pagamentoRepository.save(pagamento);
    }

    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    public Optional<Pagamento> buscarPorId(Long id) {
        return pagamentoRepository.findById(id);
    }

    public void excluir(Long id) {
        pagamentoRepository.deleteById(id);
    }

    @Transactional
    public Pagamento atualizarStatus(Long id, StatusPagamento novoStatus) {

        if (novoStatus == null) {
            throw new IllegalArgumentException(
                    "O status do pagamento é obrigatório.");
        }

        Pagamento pagamento = pagamentoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pagamento não encontrado."));

        if (pagamento.getStatus() == StatusPagamento.APROVADO &&
                novoStatus != StatusPagamento.APROVADO) {

            throw new IllegalArgumentException(
                    "Um pagamento aprovado não pode ter seu status alterado.");
        }

        Pedido pedido = pagamento.getPedido();

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "Não é possível alterar o pagamento de um pedido cancelado.");
        }

        // ─── Se está aprovando, confirma o pedido + debita estoque ───
        if (novoStatus == StatusPagamento.APROVADO) {

            if (pedido.getStatus() == StatusPedido.PENDENTE) {
                try {
                    pedidoService.confirmarPedido(pedido);
                } catch (IllegalStateException e) {
                    throw new IllegalArgumentException(
                            "Não foi possível aprovar o pagamento: "
                            + e.getMessage());
                }
            }
        }

        pagamento.setStatus(novoStatus);
        return pagamentoRepository.save(pagamento);
    }
}