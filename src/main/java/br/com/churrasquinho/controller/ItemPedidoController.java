package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.ItemPedido;
import br.com.churrasquinho.service.ItemPedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController {

    private final ItemPedidoService itemPedidoService;

    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

    @GetMapping
    public ResponseEntity<List<ItemPedido>> listarTodos() {
        return ResponseEntity.ok(itemPedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemPedido> buscarPorId(@PathVariable Long id) {
        return itemPedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ItemPedido> salvar(@RequestBody ItemPedido itemPedido) {
        return ResponseEntity.ok(itemPedidoService.salvar(itemPedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemPedido> atualizar(
            @PathVariable Long id,
            @RequestBody ItemPedido itemPedido) {

        return ResponseEntity.ok(
                itemPedidoService.atualizar(id, itemPedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        itemPedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}