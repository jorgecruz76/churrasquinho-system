package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Pedido;
import br.com.churrasquinho.enums.StatusPedido;
import br.com.churrasquinho.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pedido> salvar(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(pedidoService.salvar(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status,
            @RequestParam(required = false) String observacao,
            HttpSession session) {

        try {
            // Pega o ID do entregador logado na sessão (se for um entregador)
            Long entregadorId = null;
            Object tipo = session.getAttribute("usuarioTipo");
            
            if (tipo != null && "ENTREGADOR".equals(tipo.toString())) {
                entregadorId = (Long) session.getAttribute("usuarioId");
            }

            return ResponseEntity.ok(
                    pedidoService.atualizarStatus(id, status, entregadorId, observacao));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/entregador")
    public ResponseEntity<List<Pedido>> listarParaEntregador() {
        return ResponseEntity.ok(pedidoService.listarParaEntregador());
    }

    @GetMapping("/entregador/historico")
    public ResponseEntity<List<Pedido>> listarHistoricoParaEntregador() {
        return ResponseEntity.ok(pedidoService.listarHistoricoParaEntregador());
    }
}