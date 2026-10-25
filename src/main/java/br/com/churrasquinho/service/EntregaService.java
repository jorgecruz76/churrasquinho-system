package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Entrega;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.enums.StatusEntrega;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.repository.EntregaRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;

    public EntregaService(
            EntregaRepository entregaRepository,
            PedidoRepository pedidoRepository) {

        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Entrega salvar(Entrega entrega) {

        if (entrega.getPedido() == null ||
                entrega.getPedido().getId() == null) {

            throw new IllegalArgumentException(
                    "O pedido é obrigatório."
            );
        }

        Pedido pedido = pedidoRepository
                .findById(entrega.getPedido().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pedido não encontrado."
                        ));

        if (entrega.getId() == null &&
                entregaRepository.findAll().stream()
                        .anyMatch(e ->
                                e.getPedido().getId()
                                        .equals(pedido.getId()))) {

            throw new IllegalArgumentException(
                    "O pedido já possui uma entrega."
            );
        }

        entrega.setPedido(pedido);

        if (entrega.getStatus() == null) {
            entrega.setStatus(StatusEntrega.PENDENTE);
        }

        if (entrega.getDataCriacao() == null) {
            entrega.setDataCriacao(LocalDateTime.now());
        }

        return entregaRepository.save(entrega);
    }

    public List<Entrega> listarTodos() {
        return entregaRepository.findAll();
    }

    public Optional<Entrega> buscarPorId(Long id) {
        return entregaRepository.findById(id);
    }

    public void excluir(Long id) {
        entregaRepository.deleteById(id);
    }

    public Entrega atualizarStatus(Long id, StatusEntrega novoStatus) {

    if (novoStatus == null) {
        throw new IllegalArgumentException(
                "O status da entrega é obrigatório."
        );
    }

    Entrega entrega = entregaRepository
            .findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Entrega não encontrada."
                    ));

    StatusEntrega statusAtual = entrega.getStatus();

    if (!transicaoPermitida(statusAtual, novoStatus)) {
        throw new IllegalArgumentException(
                "Transição de status não permitida: "
                        + statusAtual + " → " + novoStatus
        );
    }

    entrega.setStatus(novoStatus);

    Pedido pedido = entrega.getPedido();

switch (novoStatus) {

    case EM_PREPARACAO:
        pedido.setStatus(StatusPedido.EM_PREPARACAO);
        break;

    case SAIU_PARA_ENTREGA:
        pedido.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        break;

    case ENTREGUE:
        pedido.setStatus(StatusPedido.ENTREGUE);
        break;

    default:
        break;
}

pedidoRepository.save(pedido);

    if (novoStatus == StatusEntrega.SAIU_PARA_ENTREGA) {
        entrega.setDataSaida(LocalDateTime.now());
    }

    if (novoStatus == StatusEntrega.ENTREGUE) {
        entrega.setDataEntrega(LocalDateTime.now());
    }

    return entregaRepository.save(entrega);
}

private boolean transicaoPermitida(
        StatusEntrega atual,
        StatusEntrega novo) {

    if (atual == StatusEntrega.PENDENTE &&
            novo == StatusEntrega.EM_PREPARACAO) {
        return true;
    }

    if (atual == StatusEntrega.EM_PREPARACAO &&
            novo == StatusEntrega.SAIU_PARA_ENTREGA) {
        return true;
    }

    if (atual == StatusEntrega.SAIU_PARA_ENTREGA &&
            novo == StatusEntrega.ENTREGUE) {
        return true;
    }

    return false;
}
}