package br.com.churrasquinho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/categorias/tela")
    public String categorias() {
        return "categorias";
    }

    @GetMapping("/produtos/tela")
    public String produtos() {
        return "produtos";
    }

    @GetMapping("/clientes/tela")
    public String clientes() {
        return "clientes";
    }

    @GetMapping("/pedidos/tela")
    public String pedidos() {
        return "pedidos";
    }

    @GetMapping("/pagamentos/tela")
    public String pagamentos() {
        return "pagamentos";
    }

    @GetMapping("/entregas/tela")
    public String entregas() {
        return "entregas";
    }
}
