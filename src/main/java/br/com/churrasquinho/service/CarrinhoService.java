package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CarrinhoService {

    public static final String ATTR_CARRINHO = "carrinho";

    private final ProdutoRepository produtoRepository;

    public CarrinhoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /**
     * Retorna o mapa de carrinho da sessão (produtoId → quantidade).
     * Cria se não existir.
     */
    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCarrinho(HttpSession session) {

        Map<Long, Integer> carrinho =
                (Map<Long, Integer>) session.getAttribute(ATTR_CARRINHO);

        if (carrinho == null) {
            carrinho = new LinkedHashMap<>();
            session.setAttribute(ATTR_CARRINHO, carrinho);
        }

        return carrinho;
    }

    /**
     * Adiciona um produto ao carrinho.
     * Lança exceção se a quantidade ultrapassar o estoque.
     */
    public void adicionar(HttpSession session, Long produtoId, int quantidade) {

        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade deve ser maior que zero.");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado."));

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new IllegalArgumentException(
                    "Produto indisponível.");
        }

        Map<Long, Integer> carrinho = getCarrinho(session);

        int quantidadeAtual = carrinho.getOrDefault(produtoId, 0);
        int novaQuantidade = quantidadeAtual + quantidade;

        if (novaQuantidade > produto.getEstoque()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: "
                            + produto.getEstoque());
        }

        carrinho.put(produtoId, novaQuantidade);
    }

    /**
     * Adiciona um produto ao carrinho ajustando ao estoque disponível.
     *
     * Retorna uma mensagem de aviso (ou null se adicionou exatamente
     * a quantidade pedida). Lança IllegalArgumentException se o produto
     * não existir, estiver inativo ou sem estoque.
     */
    public String adicionarComAjuste(
            HttpSession session, Long produtoId, int quantidade) {

        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade deve ser maior que zero.");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado."));

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new IllegalArgumentException(
                    "Produto indisponível.");
        }

        if (produto.getEstoque() == null || produto.getEstoque() <= 0) {
            throw new IllegalArgumentException(
                    "Produto sem estoque: " + produto.getNome());
        }

        Map<Long, Integer> carrinho = getCarrinho(session);

        int quantidadeAtual = carrinho.getOrDefault(produtoId, 0);
        int quantidadeDesejada = quantidadeAtual + quantidade;

        // Ajusta ao estoque disponível
        int quantidadeFinal = Math.min(
                quantidadeDesejada, produto.getEstoque());

        carrinho.put(produtoId, quantidadeFinal);

        // Se houve ajuste, retorna mensagem de aviso
        if (quantidadeFinal < quantidadeDesejada) {
            return "Estoque insuficiente para " + produto.getNome()
                    + ". Adicionamos o máximo disponível ("
                    + produto.getEstoque() + ").";
        }

        return null;
    }

    /**
     * Altera a quantidade de um produto já no carrinho.
     * Se quantidade for <= 0, remove o produto.
     */
    public void alterarQuantidade(
            HttpSession session, Long produtoId, int novaQuantidade) {

        if (novaQuantidade <= 0) {
            remover(session, produtoId);
            return;
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado."));

        if (novaQuantidade > produto.getEstoque()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: "
                            + produto.getEstoque());
        }

        Map<Long, Integer> carrinho = getCarrinho(session);
        carrinho.put(produtoId, novaQuantidade);
    }

    public void remover(HttpSession session, Long produtoId) {
        Map<Long, Integer> carrinho = getCarrinho(session);
        carrinho.remove(produtoId);
    }

    public void limpar(HttpSession session) {
        session.removeAttribute(ATTR_CARRINHO);
    }

    public int quantidadeTotal(HttpSession session) {
        return getCarrinho(session).values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public BigDecimal calcularTotal(HttpSession session) {

        Map<Long, Integer> carrinho = getCarrinho(session);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {

            Produto produto = produtoRepository
                    .findById(entry.getKey())
                    .orElse(null);

            if (produto == null) continue;

            BigDecimal subtotal = produto.getPreco()
                    .multiply(BigDecimal.valueOf(entry.getValue()));

            total = total.add(subtotal);
        }

        return total;
    }
}