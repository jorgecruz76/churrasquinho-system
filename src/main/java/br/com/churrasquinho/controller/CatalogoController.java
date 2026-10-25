package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Produto;
import br.com.churrasquinho.repository.ProdutoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CatalogoController {

    private final ProdutoRepository produtoRepository;

    public CatalogoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping({"/", "/catalogo"})
    public String catalogo(Model model) {

        List<Produto> produtos = produtoRepository.findAll()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .filter(p -> p.getEstoque() != null && p.getEstoque() > 0)
                .toList();

        model.addAttribute("produtos", produtos);

        return "catalogo";
    }
}