package br.com.churrasquinho.controller;

import br.com.churrasquinho.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cadastro")
    public String formulario() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model) {

        // Validação básica no controller
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem.");
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            return "cadastro";
        }

        try {
            usuarioService.cadastrarCliente(nome, email, senha);
            return "redirect:/login?cadastro=ok";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            return "cadastro";
        }
    }
}