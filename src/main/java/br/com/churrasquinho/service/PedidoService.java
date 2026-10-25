package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.entity.ItemPedido;
import br.com.churrasquinho.entity.Pagamento;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.enums.StatusPagamento;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.enums.TipoEntrega;
import br.com.churrasquinho.repository.ClienteRepository;
import br.com.churrasquinho.repository.PagamentoRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import br.com.churrasquinho.repository.ProdutoRepository;
import br.com.churrasquinho.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository; // ← CORRIGIDO

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            PagamentoRepository pagamentoRepository,
            UsuarioRepository usuarioRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Cria um novo pedido (via API/tela admin).
     * Recalcula subtotais e valorTotal a partir dos itens enviados,
     * ignorando valores vindos do frontend (defesa em profundidade).
     */
    public Pedido salvar(Pedido pedido) {

        if (pedido.getCliente() == null ||
                pedido.getCliente().getId() == null) {
            throw new IllegalArgumentException("O cliente é obrigatório.");
        }

        Cliente cliente = clienteRepository
                .findById(pedido.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado."));

        pedido.setCliente(cliente);

        if (pedido.getStatus() == null) {
            pedido.setStatus(StatusPedido.PENDENTE);
        } else if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalArgumentException(
                    "Um novo pedido deve ser criado com status PENDENTE.");
        }

        // ─── Recalcula subtotais e total no backend ───
        BigDecimal total = BigDecimal.ZERO;

        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {

                if (item.getProduto() == null ||
                        item.getProduto().getId() == null) {
                    throw new IllegalArgumentException(
                            "Todo item precisa de um produto.");
                }

                Produto produto = produtoRepository
                        .findById(item.getProduto().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Produto não encontrado: "
                                        + item.getProduto().getId()));

                item.setPedido(pedido);
                item.setProduto(produto);

                // Preço SEMPRE vem do banco, nunca do frontend
                item.setPrecoUnitario(produto.getPreco());

                item.calcularSubtotal();

                total = total.add(item.getSubtotal());
            }
        }

        pedido.setValorTotal(total);

        return pedidoRepository.save(pedido);
    }

    /**
     * Cria um pedido a partir do carrinho de sessão do cliente logado.
     * Estoque NÃO é baixado aqui — só na aprovação do pagamento.
     */
    @Transactional
    public Pedido criarPedidoDoCarrinho(
            Usuario usuario,
            Map<Long, Integer> carrinho) {

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }

        if (carrinho == null || carrinho.isEmpty()) {
            throw new IllegalArgumentException("Carrinho vazio.");
        }

        Cliente cliente = clienteRepository
                .findByUsuario_Id(usuario.getId())
                .orElseGet(() -> { // ← ESTA LINHA
                    Cliente novo = new Cliente();
                    novo.setNome(usuario.getNome());
                    novo.setEmail(usuario.getEmail());
                    novo.setUsuario(usuario);
                    novo.setAtivo(true);
                    return clienteRepository.save(novo);
                });

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setTipoEntrega(TipoEntrega.ENTREGA);

        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {

            Long produtoId = entry.getKey();
            Integer quantidade = entry.getValue();

            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException(
                        "Quantidade inválida para o produto " + produtoId);
            }

            Produto produto = produtoRepository
                    .findById(produtoId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + produtoId));

            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new IllegalArgumentException(
                        "Produto indisponível: " + produto.getNome());
            }

            if (produto.getEstoque() == null
                    || quantidade > produto.getEstoque()) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para " + produto.getNome()
                                + ". Disponível: " + produto.getEstoque());
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.calcularSubtotal();

            total = total.add(item.getSubtotal());

            itens.add(item);
        }

        pedido.setItens(itens);
        pedido.setValorTotal(total);

        return pedidoRepository.save(pedido);
    }

    /**
     * Confirma o pedido e debita o estoque dos itens.
     * Permite estoque negativo (oversell) — o admin repõe depois.
     */
    @Transactional
    public void confirmarPedido(Pedido pedido) {

        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException("Pedido inválido.");
        }

        Pedido pedidoAtual = pedidoRepository.findById(pedido.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        if (pedidoAtual.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalStateException(
                    "Só é possível confirmar pedidos com status PENDENTE. "
                            + "Status atual: " + pedidoAtual.getStatus());
        }

        if (pedidoAtual.getItens() == null || pedidoAtual.getItens().isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível confirmar um pedido sem itens.");
        }

        // ─── Debita o estoque de todos os itens (permite negativo) ───
        for (ItemPedido item : pedidoAtual.getItens()) {

            Produto produto = produtoRepository
                    .findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + item.getProduto().getId()));

            int estoqueAtual = produto.getEstoque() == null ? 0 : produto.getEstoque();
            produto.setEstoque(estoqueAtual - item.getQuantidade());

            produtoRepository.save(produto);
        }

        // ─── Muda o status ───
        pedidoAtual.setStatus(StatusPedido.CONFIRMADO);
        pedidoRepository.save(pedidoAtual);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    /**
     * Lista os pedidos que o entregador precisa ver:
     * status PRONTO (prontos para sair) e SAIU_PARA_ENTREGA (em rota).
     */
    @Transactional(readOnly = true)
    public List<Pedido> listarParaEntregador() {
        return pedidoRepository.findAll()
                .stream()
                .filter(p -> p.getTipoEntrega() == TipoEntrega.ENTREGA) // ← NOVO
                .filter(p -> p.getStatus() == StatusPedido.PRONTO
                        || p.getStatus() == StatusPedido.SAIU_PARA_ENTREGA)
                .sorted((a, b) -> {
                    if (a.getStatus() != b.getStatus()) {
                        return a.getStatus() == StatusPedido.PRONTO ? -1 : 1;
                    }
                    return a.getId().compareTo(b.getId());
                })
                .toList();
    }

    @Transactional
    public void excluir(Long id) {

        Pedido pedido = pedidoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um pedido que não está pendente.");
        }

        for (ItemPedido item : pedido.getItens()) {

            Produto produto = item.getProduto();

            if (produto.getEstoque() == null) {
                produto.setEstoque(0);
            }

            produto.setEstoque(
                    produto.getEstoque() + item.getQuantidade());

            produtoRepository.save(produto);
        }

        pedidoRepository.delete(pedido);
    }

    @Transactional
    public Pedido atualizarStatus(
            Long id,
            StatusPedido novoStatus,
            Long entregadorId,
            String observacao) {

        if (novoStatus == null) {
            throw new IllegalArgumentException(
                    "O status do pedido é obrigatório.");
        }

        Pedido pedido = pedidoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        StatusPedido statusAtual = pedido.getStatus();

        if (statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "O pedido já está cancelado.");
        }

        if (novoStatus == StatusPedido.CANCELADO) {

            if (statusAtual != StatusPedido.PENDENTE &&
                    statusAtual != StatusPedido.CONFIRMADO) {
                throw new IllegalArgumentException(
                        "O pedido não pode ser cancelado no status "
                                + statusAtual + ".");
            }

            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                if (produto.getEstoque() == null)
                    produto.setEstoque(0);
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }

            pedido.setStatus(StatusPedido.CANCELADO);
            return pedidoRepository.save(pedido);
        }

        if (statusAtual == StatusPedido.PENDENTE &&
                novoStatus == StatusPedido.CONFIRMADO &&
                (pedido.getItens() == null || pedido.getItens().isEmpty())) {
            throw new IllegalArgumentException(
                    "Não é possível confirmar um pedido sem itens.");
        }

        if (statusAtual == StatusPedido.CONFIRMADO &&
                novoStatus == StatusPedido.EM_PREPARACAO) {

            Pagamento pagamento = pagamentoRepository
                    .findByPedido_Id(pedido.getId())
                    .orElse(null);

            if (pagamento == null) {
                throw new IllegalArgumentException(
                        "Não é possível avançar para EM PREPARAÇÃO. "
                                + "Este pedido ainda não possui pagamento cadastrado. "
                                + "Acesse Pagamentos e crie um pagamento aprovado.");
            }

            if (pagamento.getStatus() != StatusPagamento.APROVADO) {
                throw new IllegalArgumentException(
                        "Não é possível avançar para EM PREPARAÇÃO. "
                                + "O pagamento atual está com status "
                                + pagamento.getStatus() + ". "
                                + "Aprove o pagamento na tela de Pagamentos antes de continuar.");
            }
        }

        boolean transicaoValida = (statusAtual == StatusPedido.PENDENTE && novoStatus == StatusPedido.CONFIRMADO)
                || (statusAtual == StatusPedido.CONFIRMADO && novoStatus == StatusPedido.EM_PREPARACAO)
                || (statusAtual == StatusPedido.EM_PREPARACAO && novoStatus == StatusPedido.PRONTO)
                || (statusAtual == StatusPedido.PRONTO && novoStatus == StatusPedido.SAIU_PARA_ENTREGA)
                || (statusAtual == StatusPedido.SAIU_PARA_ENTREGA && novoStatus == StatusPedido.ENTREGUE);

        if (!transicaoValida) {
            throw new IllegalArgumentException(
                    "Não é possível alterar o pedido de "
                            + statusAtual + " para " + novoStatus + ".");
        }

        // ─── Registra saída + quem pegou ───
        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA) {
            pedido.setDataSaida(LocalDateTime.now());

            if (entregadorId != null) {
                Usuario entregador = usuarioRepository
                        .findById(entregadorId)
                        .orElse(null);
                pedido.setEntregador(entregador);
            }
        }

        // ─── Registra entrega + observação ───
        if (novoStatus == StatusPedido.ENTREGUE) {
            pedido.setDataEntrega(LocalDateTime.now());

            if (observacao != null && !observacao.isBlank()) {
                pedido.setObservacaoEntrega(observacao.trim());
            }
        }

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Long id, StatusPedido novoStatus) {
        return atualizarStatus(id, novoStatus, null, null);
    }

    public List<Pedido> listarHistoricoParaEntregador() {
        return pedidoRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == StatusPedido.ENTREGUE)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .toList();
    }
}