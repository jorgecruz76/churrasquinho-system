package br.com.churrasquinho.controller;

import br.com.churrasquinho.dto.ItemCarrinhoDTO;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.repository.ProdutoRepository;
import br.com.churrasquinho.repository.UsuarioRepository;
import br.com.churrasquinho.service.CarrinhoService;
import br.com.churrasquinho.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final ProdutoRepository produtoRepository;
    private final PedidoService pedidoService;
    private final UsuarioRepository usuarioRepository;

    public CarrinhoController(
            CarrinhoService carrinhoService,
            ProdutoRepository produtoRepository,
            PedidoService pedidoService,
            UsuarioRepository usuarioRepository) {
        this.carrinhoService = carrinhoService;
        this.produtoRepository = produtoRepository;
        this.pedidoService = pedidoService;
        this.usuarioRepository = usuarioRepository;
    }

    /** Exibe a tela do carrinho. */
    @GetMapping
    public String verCarrinho(HttpSession session, Model model) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login?redirect=/carrinho";
        }

        Map<Long, Integer> carrinho = carrinhoService.getCarrinho(session);

        List<ItemCarrinhoDTO> itens = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : carrinho.entrySet()) {
            produtoRepository.findById(entry.getKey())
                    .ifPresent(produto -> itens.add(
                            new ItemCarrinhoDTO(produto, entry.getValue())));
        }

        model.addAttribute("itens", itens);
        model.addAttribute("total", carrinhoService.calcularTotal(session));

        return "carrinho";
    }

    /** Adiciona um produto ao carrinho. */
    @PostMapping("/adicionar/{produtoId}")
    public String adicionar(
            @PathVariable Long produtoId,
            @RequestParam(defaultValue = "1") Integer quantidade,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ─── Se não estiver logado, guarda a intenção e manda pro login ───
        if (session.getAttribute("usuarioId") == null) {
            session.setAttribute("adicionarAposLogin_produtoId", produtoId);
            session.setAttribute("adicionarAposLogin_quantidade", quantidade);

            return "redirect:/login?redirect=/carrinho/adicionar-pendente";
        }

        // ─── Já logado: adiciona normalmente ───
        try {
            carrinhoService.adicionar(session, produtoId, quantidade);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Produto adicionado ao carrinho.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/catalogo";
    }

    /**
     * Executa o "adicionar ao carrinho" que ficou pendente antes do login.
     * Chamado logo após o login, quando o usuário tentou adicionar deslogado.
     */
    @GetMapping("/adicionar-pendente")
    public String adicionarPendente(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        Long produtoId = (Long) session.getAttribute("adicionarAposLogin_produtoId");
        Integer quantidade = (Integer) session.getAttribute("adicionarAposLogin_quantidade");

        // Limpa a intenção da sessão
        session.removeAttribute("adicionarAposLogin_produtoId");
        session.removeAttribute("adicionarAposLogin_quantidade");

        // Se não tinha nada pendente, vai pro catálogo
        if (produtoId == null) {
            return "redirect:/catalogo";
        }

        try {
            String aviso = carrinhoService.adicionarComAjuste(
                    session, produtoId,
                    quantidade != null ? quantidade : 1);

            if (aviso != null) {
                redirectAttributes.addFlashAttribute("aviso", aviso);
            } else {
                redirectAttributes.addFlashAttribute("sucesso",
                        "Produto adicionado ao carrinho.");
            }

        } catch (IllegalArgumentException e) {
            // Produto não encontrado, inativo, sem estoque, etc.
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/catalogo";
        }

        return "redirect:/carrinho";
    }

    /** Remove um produto do carrinho. */
    @PostMapping("/remover/{produtoId}")
    public String remover(
            @PathVariable Long produtoId,
            HttpSession session) {

        carrinhoService.remover(session, produtoId);
        return "redirect:/carrinho";
    }

    /** Altera a quantidade de um produto já no carrinho. */
    @PostMapping("/alterar/{produtoId}")
    public String alterarQuantidade(
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login?redirect=/carrinho";
        }

        try {
            carrinhoService.alterarQuantidade(session, produtoId, quantidade);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/carrinho";
    }

    /** Finaliza o pedido — cria Pedido PENDENTE e limpa o carrinho. */
    @PostMapping("/finalizar")
    public String finalizar(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login?redirect=/carrinho";
        }

        Map<Long, Integer> carrinho = carrinhoService.getCarrinho(session);

        if (carrinho.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro",
                    "Carrinho vazio.");
            return "redirect:/carrinho";
        }

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário não encontrado."));

            Pedido pedido = pedidoService.criarPedidoDoCarrinho(
                    usuario, carrinho);

            carrinhoService.limpar(session);

            return "redirect:/meus-dados?pedidoId=" + pedido.getId();
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho";
        }
    }

    /** Tela de confirmação após finalizar. */
    @GetMapping("/pedido-criado/{id}")
    public String pedidoCriado(@PathVariable Long id, Model model) {
        model.addAttribute("pedidoId", id);
        return "pedido-criado";
    }

    /** Esvazia o carrinho inteiro. */
    @PostMapping("/limpar")
    public String limparCarrinho(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login?redirect=/carrinho";
        }

        carrinhoService.limpar(session);
        redirectAttributes.addFlashAttribute("sucesso",
                "Carrinho esvaziado.");

        return "redirect:/carrinho";
    }
}