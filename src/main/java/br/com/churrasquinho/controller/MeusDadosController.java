package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.enums.TipoEntrega;
import br.com.churrasquinho.repository.ClienteRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MeusDadosController {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public MeusDadosController(
            ClienteRepository clienteRepository,
            PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Tela "Meus dados" — mostra após finalizar o pedido.
     * O cliente escolhe entre ENTREGA ou RETIRADA.
     */
    @Transactional(readOnly = true)
    @GetMapping("/meus-dados")
    public String tela(
            @RequestParam(required = false) Long pedidoId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login?redirect=/meus-dados";
        }

        Cliente cliente = clienteRepository.findByUsuario_Id(usuarioId)
                .orElse(null);

        // Se já tem endereço E não veio pedidoId, redireciona direto
        if (cliente != null && temEnderecoCompleto(cliente) && pedidoId == null) {
            redirectAttributes.addFlashAttribute("sucesso",
                    "Seus dados já estão completos.");
            return "redirect:/meus-pedidos";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("pedidoId", pedidoId);
        model.addAttribute("temEndereco", cliente != null && temEnderecoCompleto(cliente));

        return "meus-dados";
    }

    /**
     * Salva os dados (endereço ou confirmação de retirada).
     */
    @Transactional
    @PostMapping("/meus-dados")
    public String salvar(
            @RequestParam Long pedidoId,
            @RequestParam TipoEntrega tipoEntrega,
            @RequestParam(required = false) String logradouro,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String cep,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login?redirect=/meus-dados";
        }

        Cliente cliente = clienteRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado."));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        // ─── Se for ENTREGA, valida endereço ───
        if (tipoEntrega == TipoEntrega.ENTREGA) {

            if (logradouro == null || logradouro.isBlank()
                    || numero == null || numero.isBlank()
                    || bairro == null || bairro.isBlank()
                    || cidade == null || cidade.isBlank()
                    || estado == null || estado.isBlank()
                    || cep == null || cep.isBlank()) {

                redirectAttributes.addFlashAttribute("erro",
                        "Preencha todos os campos de endereço ou escolha 'Retirar na loja'.");
                return "redirect:/meus-dados?pedidoId=" + pedidoId;
            }

            cliente.setLogradouro(logradouro.trim());
            cliente.setNumero(numero.trim());
            cliente.setBairro(bairro.trim());
            cliente.setCidade(cidade.trim());
            cliente.setEstado(estado.trim().toUpperCase());
            cliente.setCep(cep.trim());

            clienteRepository.save(cliente);
        }

        // ─── Atualiza o tipo de entrega do pedido ───
        pedido.setTipoEntrega(tipoEntrega);
        pedidoRepository.save(pedido);

        return "redirect:/pagamento/" + pedidoId;
    }

    /**
     * "Pular e pagar agora" — não preenche nada, mas mantém o tipo como ENTREGA
     * (ou o cliente já escolheu RETIRADA antes).
     */
    @Transactional
    @PostMapping("/meus-dados/pular")
    public String pular(
            @RequestParam Long pedidoId,
            @RequestParam TipoEntrega tipoEntrega,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login?redirect=/meus-dados";
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado."));

        pedido.setTipoEntrega(tipoEntrega);
        pedidoRepository.save(pedido);

        redirectAttributes.addFlashAttribute("aviso",
                "Você pode completar seus dados depois em 'Meus pedidos'.");

        return "redirect:/pagamento/" + pedidoId;
    }

    private boolean temEnderecoCompleto(Cliente c) {
        return c.getLogradouro() != null && !c.getLogradouro().isBlank()
                && c.getNumero() != null && !c.getNumero().isBlank()
                && c.getBairro() != null && !c.getBairro().isBlank()
                && c.getCidade() != null && !c.getCidade().isBlank()
                && c.getEstado() != null && !c.getEstado().isBlank()
                && c.getCep() != null && !c.getCep().isBlank();
    }
}