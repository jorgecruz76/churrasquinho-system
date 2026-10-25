package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Pagamento;
import br.com.churrasquinho.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.churrasquinho.enums.StatusPagamento;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping
    public ResponseEntity<List<Pagamento>> listarTodos() {
        return ResponseEntity.ok(pagamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscarPorId(@PathVariable Long id) {
        return pagamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pagamento> salvar(@RequestBody Pagamento pagamento) {
        return ResponseEntity.ok(pagamentoService.salvar(pagamento));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pagamento> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPagamento status) {

        return ResponseEntity.ok(
                pagamentoService.atualizarStatus(id, status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pagamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}