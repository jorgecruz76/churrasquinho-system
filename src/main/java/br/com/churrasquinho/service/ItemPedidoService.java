package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.ItemPedido;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.repository.ItemPedidoRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import br.com.churrasquinho.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ItemPedidoService {

        private final ItemPedidoRepository itemPedidoRepository;
        private final PedidoRepository pedidoRepository;
        private final ProdutoRepository produtoRepository;

        public ItemPedidoService(
                        ItemPedidoRepository itemPedidoRepository,
                        PedidoRepository pedidoRepository,
                        ProdutoRepository produtoRepository) {

                this.itemPedidoRepository = itemPedidoRepository;
                this.pedidoRepository = pedidoRepository;
                this.produtoRepository = produtoRepository;
        }

        public ItemPedido salvar(ItemPedido itemPedido) {

                if (itemPedido.getPedido() == null ||
                                itemPedido.getPedido().getId() == null) {
                        throw new IllegalArgumentException("O pedido é obrigatório.");
                }

                if (itemPedido.getProduto() == null ||
                                itemPedido.getProduto().getId() == null) {
                        throw new IllegalArgumentException("O produto é obrigatório.");
                }

                if (itemPedido.getQuantidade() == null ||
                                itemPedido.getQuantidade() <= 0) {
                        throw new IllegalArgumentException(
                                        "A quantidade deve ser maior que zero.");
                }

                Pedido pedido = pedidoRepository
                                .findById(itemPedido.getPedido().getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Pedido não encontrado."));
                if (pedido.getStatus() != StatusPedido.PENDENTE) {
                        throw new IllegalArgumentException(
                                        "Não é possível adicionar itens a um pedido que não está pendente.");
                }

                Produto produto = produtoRepository
                                .findById(itemPedido.getProduto().getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Produto não encontrado."));

                if (!Boolean.TRUE.equals(produto.getAtivo())) {
                        throw new IllegalArgumentException(
                                        "O produto está inativo.");
                }

                if (produto.getEstoque() == null ||
                                produto.getEstoque() < itemPedido.getQuantidade()) {
                        throw new IllegalArgumentException(
                                        "Estoque insuficiente para o produto.");
                }

                itemPedido.setPedido(pedido);
                itemPedido.setProduto(produto);

                // baixa o estoque do produto de acordo com a quantidade do item do pedido
                produto.setEstoque(
                                produto.getEstoque() - itemPedido.getQuantidade());

                produtoRepository.save(produto);

                // O preço utilizado é sempre o preço atual do produto.
                itemPedido.setPrecoUnitario(produto.getPreco());

                // Calcula o subtotal do item.
                itemPedido.calcularSubtotal();

                ItemPedido itemSalvo = itemPedidoRepository.save(itemPedido);

                // Atualiza o valor total do pedido.
                recalcularTotalPedido(pedido.getId());

                return itemSalvo;
        }

        private void recalcularTotalPedido(Long pedidoId) {

                List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(pedidoId);

                BigDecimal total = itens.stream()
                                .map(ItemPedido::getSubtotal)
                                .filter(subtotal -> subtotal != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Pedido pedido = pedidoRepository
                                .findById(pedidoId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Pedido não encontrado."));

                pedido.setValorTotal(total);

                pedidoRepository.save(pedido);
        }

        public List<ItemPedido> listarTodos() {
                return itemPedidoRepository.findAll();
        }

        public Optional<ItemPedido> buscarPorId(Long id) {
                return itemPedidoRepository.findById(id);
        }

        public ItemPedido atualizar(Long id, ItemPedido itemPedido) {

                ItemPedido itemExistente = itemPedidoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Item do pedido não encontrado."));

                Pedido pedido = itemExistente.getPedido();

                if (pedido.getStatus() != StatusPedido.PENDENTE) {
                        throw new IllegalArgumentException(
                                        "Não é possível alterar itens de um pedido que não está pendente.");
                }

                if (itemPedido.getQuantidade() == null ||
                                itemPedido.getQuantidade() <= 0) {
                        throw new IllegalArgumentException(
                                        "A quantidade deve ser maior que zero.");
                }

                int quantidadeAntiga = itemExistente.getQuantidade();
                int quantidadeNova = itemPedido.getQuantidade();

                Produto produto = itemExistente.getProduto();

                int diferenca = quantidadeNova - quantidadeAntiga;

                // Aumentou a quantidade: precisa retirar mais estoque.
                if (diferenca > 0) {

                        if (produto.getEstoque() == null ||
                                        produto.getEstoque() < diferenca) {
                                throw new IllegalArgumentException(
                                                "Estoque insuficiente para aumentar a quantidade.");
                        }

                        produto.setEstoque(
                                        produto.getEstoque() - diferenca);

                        // Diminuiu a quantidade: devolve a diferença ao estoque.
                } else if (diferenca < 0) {

                        produto.setEstoque(
                                        produto.getEstoque() + Math.abs(diferenca));
                }

                produtoRepository.save(produto);

                itemExistente.setQuantidade(quantidadeNova);
                itemExistente.setPrecoUnitario(produto.getPreco());
                itemExistente.calcularSubtotal();

                ItemPedido itemAtualizado = itemPedidoRepository.save(itemExistente);

                recalcularTotalPedido(
                                itemExistente.getPedido().getId());

                return itemAtualizado;
        }

        public void excluir(Long id) {

                ItemPedido itemPedido = itemPedidoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Item do pedido não encontrado."));

                Pedido pedido = itemPedido.getPedido();

                if (pedido.getStatus() != StatusPedido.PENDENTE) {
                        throw new IllegalArgumentException(
                                        "Não é possível excluir itens de um pedido que não está pendente.");
                }

                Long pedidoId = itemPedido.getPedido().getId();

                Produto produto = itemPedido.getProduto();

                produto.setEstoque(
                                produto.getEstoque() + itemPedido.getQuantidade());

                produtoRepository.save(produto);

                itemPedidoRepository.deleteById(id);

                // Atualiza o total depois da exclusão.
                recalcularTotalPedido(pedidoId);
        }
}