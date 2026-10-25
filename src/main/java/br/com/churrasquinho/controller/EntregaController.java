package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Entrega;
import br.com.churrasquinho.service.EntregaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.churrasquinho.enums.StatusEntrega;

import java.util.List;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public ResponseEntity<List<Entrega>> listarTodos() {
        return ResponseEntity.ok(
                entregaService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrega> buscarPorId(
            @PathVariable Long id) {

        return entregaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Entrega> salvar(
            @RequestBody Entrega entrega) {

        return ResponseEntity.ok(
                entregaService.salvar(entrega)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        entregaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
public ResponseEntity<Entrega> atualizarStatus(
        @PathVariable Long id,
        @RequestParam StatusEntrega status) {

    return ResponseEntity.ok(
            entregaService.atualizarStatus(id, status)
    );
}
}