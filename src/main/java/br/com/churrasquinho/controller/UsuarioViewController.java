package br.com.churrasquinho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioViewController {

    @GetMapping("/usuarios/tela")
    public String tela() {
        return "usuarios";
    }
}