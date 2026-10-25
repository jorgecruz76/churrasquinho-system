package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {

        Object usuarioId = session.getAttribute("usuarioId");

        if (usuarioId != null) {
            Object tipo = session.getAttribute("usuarioTipo");

            if (tipo != null) {
                String tipoStr = tipo.toString();
                if ("ADM".equals(tipoStr)) return "redirect:/admin";
                if ("ENTREGADOR".equals(tipoStr)) return "redirect:/entregador";
            }

            return "redirect:/catalogo";
        }

        return "login";
    }

    @PostMapping("/login")
    public String autenticar(
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String redirect,
            HttpSession session) {

        try {
            Usuario usuario = usuarioService.autenticar(email, senha);

            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNome", usuario.getNome());
            session.setAttribute("usuarioTipo", usuario.getTipo());

            // ─── Só aceita redirect se for rota segura (GET) ───
            if (isRedirectSeguro(redirect)) {
                return "redirect:" + redirect;
            }

            switch (usuario.getTipo()) {

                case ADM:
                    return "redirect:/admin";

                case ENTREGADOR:
                    return "redirect:/entregador";

                case CLIENTE:
                default:
                    return "redirect:/catalogo";
            }

        } catch (IllegalArgumentException e) {
            return "redirect:/login?erro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Verifica se o redirect é uma rota segura (só GET).
     * Bloqueia rotas que só aceitam POST, pra evitar 405.
     */
    private boolean isRedirectSeguro(String redirect) {

        if (redirect == null || redirect.isBlank()) {
            return false;
        }

        // Precisa começar com "/" (evita open redirect)
        if (!redirect.startsWith("/")) {
            return false;
        }

        // Bloqueia rotas que só aceitam POST
        if (redirect.startsWith("/carrinho/adicionar")
                && !redirect.equals("/carrinho/adicionar-pendente")) {
            return false;
        }

        if (redirect.startsWith("/carrinho/remover")
                || redirect.startsWith("/carrinho/alterar")
                || redirect.startsWith("/carrinho/finalizar")
                || redirect.startsWith("/carrinho/limpar")
                || redirect.startsWith("/pagamento/")
                || redirect.startsWith("/pedidos")
                || redirect.startsWith("/usuarios")) {
            return false;
        }

        // Rotas permitidas como destino de redirect
        return redirect.equals("/catalogo")
                || redirect.equals("/carrinho")
                || redirect.equals("/carrinho/adicionar-pendente")
                || redirect.equals("/meus-pedidos")
                || redirect.equals("/admin")
                || redirect.equals("/entregador")
                || redirect.startsWith("/carrinho/pedido-criado");
    }
}