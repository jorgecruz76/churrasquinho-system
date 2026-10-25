package br.com.churrasquinho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EntregadorViewController {

    @GetMapping("/entregador")
    public String tela() {
        return "entregador";
    }
}