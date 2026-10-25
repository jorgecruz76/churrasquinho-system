package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.repository.ClienteRepository;
import br.com.churrasquinho.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MeusPedidosController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public MeusPedidosController(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    @GetMapping("/meus-pedidos")
    public String meusPedidos(HttpSession session, Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login?redirect=/meus-pedidos";
        }

        Cliente cliente = clienteRepository
                .findByUsuario_Id(usuarioId)
                .orElse(null);

        List<Pedido> pedidos = cliente == null
                ? List.of()
                : pedidoRepository.findByCliente_IdOrderByIdDesc(cliente.getId());

        model.addAttribute("pedidos", pedidos);

        return "meus-pedidos";
    }
}