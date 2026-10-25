package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Categoria;
import br.com.churrasquinho.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import br.com.churrasquinho.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    public Categoria salvar(Categoria categoria) {
        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome.");
        }

        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria atualizar(Long id, Categoria categoria) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        if (!categoriaExistente.getNome().equals(categoria.getNome())
                && categoriaRepository.existsByNome(categoria.getNome())) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome.");
        }

        categoriaExistente.setNome(categoria.getNome());
        categoriaExistente.setDescricao(categoria.getDescricao());

        if (categoria.getAtivo() == null) {
            throw new IllegalArgumentException("O status ativo da categoria é obrigatório.");
        }

        categoriaExistente.setAtivo(categoria.getAtivo());

        return categoriaRepository.save(categoriaExistente);
    }

    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }

        if (produtoRepository.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir uma categoria que possui produtos.");
        }

        categoriaRepository.deleteById(id);
    }
}