package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.entity.Pagamento;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.enums.FormaPagamento;
import br.com.churrasquinho.enums.StatusPagamento;
import br.com.churrasquinho.repository.ClienteRepository;
import br.com.churrasquinho.repository.PagamentoRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import br.com.churrasquinho.service.PagamentoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/pagamento")
public class PagamentoViewController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoService pagamentoService;

    public PagamentoViewController(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            PagamentoRepository pagamentoRepository,
            PagamentoService pagamentoService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoService = pagamentoService;
    }

    /** Tela de pagamento do pedido. */
    @Transactional(readOnly = true)
    @GetMapping("/{pedidoId}")
    public String telaPagamento(
            @PathVariable Long pedidoId,
            HttpSession session,
            Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login?redirect=/pagamento/" + pedidoId;
        }

        Cliente cliente = clienteRepository.findByUsuario_Id(usuarioId)
                .orElse(null);

        if (cliente == null) {
            return "redirect:/meus-pedidos";
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElse(null);

        if (pedido == null ||
                !pedido.getCliente().getId().equals(cliente.getId())) {
            return "redirect:/meus-pedidos";
        }

        Optional<Pagamento> pagamentoExistente =
                pagamentoRepository.findByPedido_Id(pedidoId);

        if (pagamentoExistente.isPresent() &&
                pagamentoExistente.get().getStatus() == StatusPagamento.APROVADO) {
            return "redirect:/meus-pedidos";
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("formas", FormaPagamento.values());

        return "pagamento";
    }

    /** Confirma o pagamento (cria + aprova em um passo). */
    @PostMapping("/{pedidoId}/confirmar")
    public String confirmar(
            @PathVariable Long pedidoId,
            @RequestParam FormaPagamento formaPagamento,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login?redirect=/pagamento/" + pedidoId;
        }

        try {
            Pagamento pagamento = new Pagamento();
            Pedido pedidoRef = new Pedido();
            pedidoRef.setId(pedidoId);
            pagamento.setPedido(pedidoRef);
            pagamento.setFormaPagamento(formaPagamento);

            Pagamento salvo = pagamentoService.salvar(pagamento);

            pagamentoService.atualizarStatus(salvo.getId(),
                    StatusPagamento.APROVADO);

            redirectAttributes.addFlashAttribute("sucesso",
                    "Pagamento aprovado com sucesso!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/meus-pedidos";
    }
}