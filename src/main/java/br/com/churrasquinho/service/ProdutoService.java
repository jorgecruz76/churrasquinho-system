package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Categoria;
import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.repository.CategoriaRepository;
import br.com.churrasquinho.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import br.com.churrasquinho.repository.ItemPedidoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public Produto salvar(Produto produto) {
        if (produto.getCategoria() == null ||
                produto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }

        Categoria categoria = categoriaRepository
                .findById(produto.getCategoria().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        if (produto.getId() != null) {
            Produto produtoExistente = produtoRepository.findById(produto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));

            if (itemPedidoRepository.existsByProdutoId(produto.getId())
                    && !produtoExistente.getCategoria().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException(
                        "Não é possível alterar a categoria de um produto que já possui pedidos.");
            }
        }

        produto.setCategoria(categoria);
        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public void excluir(Long id) {

        if (!produtoRepository.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }

        if (itemPedidoRepository.existsByProdutoId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um produto que já possui pedidos.");
        }

        produtoRepository.deleteById(id);
    }
}