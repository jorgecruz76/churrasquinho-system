package br.com.churrasquinho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping("/admin")
    public String admin() {
        return "admin"; // → templates/admin.html
    }

    @GetMapping("/imprimir-pedidos")
    public String imprimirPedidos() {
        return "imprimir-pedidos";
    }

}