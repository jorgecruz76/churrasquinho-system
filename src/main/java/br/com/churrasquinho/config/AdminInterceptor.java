package br.com.churrasquinho.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();
        String metodo = request.getMethod();

        // ─── Rotas públicas ───
        boolean publico = uri.equals("/")
                || uri.equals("/catalogo")
                || uri.startsWith("/login")
                || uri.startsWith("/logout")
                || uri.startsWith("/cadastro")
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/webjars")
                || uri.startsWith("/favicon")
                || uri.startsWith("/error");

        if (publico) {
            return true;
        }

        // ─── API GET pública (catálogo do cliente) ───
        boolean apiPublicaGet = "GET".equalsIgnoreCase(metodo)
                && (uri.startsWith("/produtos")
                    || uri.startsWith("/categorias"));

        if (apiPublicaGet) {
            return true;
        }

        // ─── Rotas que exigem apenas login (qualquer tipo) ───
        // EXCEÇÃO: /carrinho/adicionar passa mesmo deslogado,
        // porque o CarrinhoController trata a intenção pendente.
        boolean exigeLogin = (uri.startsWith("/carrinho")
                                && !uri.startsWith("/carrinho/adicionar"))
                || uri.startsWith("/meus-pedidos")
                || uri.startsWith("/meus-dados") 
                || uri.startsWith("/pagamento");

        // ─── Rotas exclusivas do ENTREGADOR ───
        boolean exigeEntregador = uri.equals("/entregador")
                || uri.equals("/pedidos/entregador");

        // ─── PATCH /pedidos/{id}/status (ADM ou ENTREGADOR) ───
        boolean patchStatusPedido = "PATCH".equalsIgnoreCase(metodo)
                && uri.matches("^/pedidos/\\d+/status$");

        // ─── Rotas que exigem ADM ───
        boolean exigeAdm = uri.equals("/admin")
                || uri.startsWith("/usuarios")
                || uri.startsWith("/clientes")
                || (uri.startsWith("/pedidos")
                        && !uri.equals("/pedidos/entregador")
                        && !patchStatusPedido)
                || uri.startsWith("/pagamentos")
                || uri.startsWith("/entregas")
                || uri.startsWith("/produtos/tela")
                || uri.startsWith("/imprimir-pedidos")
                || uri.startsWith("/categorias/tela");

        HttpSession session = request.getSession(false);

        // ─── Verificação: exige login (qualquer tipo) ───
        if (exigeLogin) {
            if (session == null
                    || session.getAttribute("usuarioId") == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }
            return true;
        }

        // ─── Verificação: PATCH /pedidos/{id}/status ───
        if (patchStatusPedido) {

            if (session == null
                    || session.getAttribute("usuarioId") == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }

            Object tipo = session.getAttribute("usuarioTipo");

            if (tipo == null) {
                response.sendRedirect("/catalogo");
                return false;
            }

            String tipoStr = tipo.toString();

            if ("ADM".equals(tipoStr) || "ENTREGADOR".equals(tipoStr)) {
                return true;
            }

            response.sendRedirect("/catalogo");
            return false;
        }

        // ─── Verificação: exige ENTREGADOR ───
        if (exigeEntregador) {

            if (session == null
                    || session.getAttribute("usuarioId") == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }

            Object tipo = session.getAttribute("usuarioTipo");

            if (tipo == null || !"ENTREGADOR".equals(tipo.toString())) {
                response.sendRedirect("/catalogo");
                return false;
            }

            return true;
        }

        // ─── Verificação: exige ADM ───
        if (exigeAdm) {

            if (session == null
                    || session.getAttribute("usuarioId") == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }

            Object tipo = session.getAttribute("usuarioTipo");

            if (tipo == null || !"ADM".equals(tipo.toString())) {
                response.sendRedirect("/catalogo");
                return false;
            }
        }

        return true;
    }
}