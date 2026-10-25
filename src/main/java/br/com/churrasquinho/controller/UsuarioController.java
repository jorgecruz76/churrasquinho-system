package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.enums.TipoUsuario;
import br.com.churrasquinho.repository.UsuarioRepository;
import br.com.churrasquinho.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista todos os usuários — só ADM. */
    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {

        if (!isAdm(session)) {
            return ResponseEntity.status(403)
                    .body("Apenas ADM pode listar usuários.");
        }

        List<Usuario> usuarios = usuarioRepository.findAll();
        usuarios.forEach(u -> u.setSenha(null));
        return ResponseEntity.ok(usuarios);
    }

    /** Cria usuário — só ADM. */
    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody Usuario usuario,
            HttpSession session) {

        if (!isAdm(session)) {
            return ResponseEntity.status(403)
                    .body("Apenas ADM pode criar usuários.");
        }

        try {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                throw new IllegalArgumentException("E-mail é obrigatório.");
            }

            String email = usuario.getEmail().trim().toLowerCase();

            if (usuarioRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException(
                        "Já existe um usuário com este e-mail.");
            }

            if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
                throw new IllegalArgumentException(
                        "A senha deve ter no mínimo 6 caracteres.");
            }

            if (usuario.getTipo() == null) {
                throw new IllegalArgumentException("Tipo é obrigatório.");
            }

            usuario.setEmail(email);
            usuario.setAtivo(true);

            Usuario salvo = usuarioService.salvar(usuario);
            salvo.setSenha(null);
            return ResponseEntity.ok(salvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Edita usuário — só ADM. */
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable Long id,
            @RequestBody Usuario dados,
            HttpSession session) {

        if (!isAdm(session)) {
            return ResponseEntity.status(403)
                    .body("Apenas ADM pode editar usuários.");
        }

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário não encontrado."));

            // Não permite editar o próprio tipo
            Long usuarioLogadoId = (Long) session.getAttribute("usuarioId");
            if (usuarioLogadoId != null
                    && usuarioLogadoId.equals(id)
                    && dados.getTipo() != null
                    && dados.getTipo() != usuario.getTipo()) {
                throw new IllegalArgumentException(
                        "Você não pode alterar seu próprio tipo.");
            }

            if (dados.getNome() != null && !dados.getNome().isBlank()) {
                usuario.setNome(dados.getNome().trim());
            }

            if (dados.getEmail() != null && !dados.getEmail().isBlank()) {
                String novoEmail = dados.getEmail().trim().toLowerCase();

                if (!novoEmail.equals(usuario.getEmail())) {
                    if (usuarioRepository.findByEmail(novoEmail).isPresent()) {
                        throw new IllegalArgumentException(
                                "Já existe um usuário com este e-mail.");
                    }
                    usuario.setEmail(novoEmail);
                }
            }

            if (dados.getTipo() != null) {
                usuario.setTipo(dados.getTipo());
            }

            if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
                if (dados.getSenha().length() < 6) {
                    throw new IllegalArgumentException(
                            "A senha deve ter no mínimo 6 caracteres.");
                }
                usuario.setSenha(passwordEncoder.encode(dados.getSenha()));
            }

            Usuario salvo = usuarioRepository.save(usuario);
            salvo.setSenha(null);
            return ResponseEntity.ok(salvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Ativa / desativa usuário — só ADM.
     * Regras:
     * - ADM não pode desativar a si mesmo
     * - ADM não pode desativar o último ADM ativo do sistema
     */
    @PatchMapping("/{id}/ativo")
    public ResponseEntity<?> alternarAtivo(
            @PathVariable Long id,
            @RequestParam Boolean ativo,
            HttpSession session) {

        if (!isAdm(session)) {
            return ResponseEntity.status(403)
                    .body("Apenas ADM pode ativar/desativar usuários.");
        }

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário não encontrado."));

            Long usuarioLogadoId = (Long) session.getAttribute("usuarioId");

            // ─── Não pode desativar a si mesmo ───
            if (usuarioLogadoId != null
                    && usuarioLogadoId.equals(id)
                    && !ativo) {
                throw new IllegalArgumentException(
                        "Você não pode desativar a si mesmo.");
            }

            // ─── Não pode desativar o último ADM ativo ───
            if (usuario.getTipo() == TipoUsuario.ADM && !ativo) {

                long totalAdmsAtivos = usuarioRepository.findAll().stream()
                        .filter(u -> u.getTipo() == TipoUsuario.ADM)
                        .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                        .count();

                if (totalAdmsAtivos <= 1) {
                    throw new IllegalArgumentException(
                            "Não é possível desativar o último ADM ativo.");
                }
            }

            usuario.setAtivo(ativo);
            Usuario salvo = usuarioRepository.save(usuario);
            salvo.setSenha(null);
            return ResponseEntity.ok(salvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Exclui usuário — só ADM.
     * Regras:
     * - ADM não pode excluir a si mesmo se for o último ADM ativo
     * - ADM não pode excluir outro ADM se for o último ADM ativo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdm(session)) {
            return ResponseEntity.status(403)
                    .body("Apenas ADM pode excluir usuários.");
        }

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário não encontrado."));

            Long usuarioLogadoId = (Long) session.getAttribute("usuarioId");
            boolean autoExclusao = usuarioLogadoId != null
                    && usuarioLogadoId.equals(id);

            // ─── Auto-exclusão: só permite se houver OUTRO ADM ativo ───
            if (autoExclusao) {

                long totalAdmsAtivos = usuarioRepository.findAll().stream()
                        .filter(u -> u.getTipo() == TipoUsuario.ADM)
                        .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                        .count();

                if (totalAdmsAtivos <= 1) {
                    throw new IllegalArgumentException(
                            "Não é possível excluir o último ADM ativo. "
                            + "Crie outro ADM antes de se excluir.");
                }
            }

            // ─── Se o alvo for ADM (outro), verifica se não é o último ───
            if (!autoExclusao && usuario.getTipo() == TipoUsuario.ADM) {

                long totalAdms = usuarioRepository.findAll().stream()
                        .filter(u -> u.getTipo() == TipoUsuario.ADM)
                        .count();

                if (totalAdms <= 1) {
                    throw new IllegalArgumentException(
                            "Não é possível excluir o último ADM do sistema.");
                }
            }

            usuarioRepository.delete(usuario);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Helper ───
    private boolean isAdm(HttpSession session) {
        Object tipo = session.getAttribute("usuarioTipo");
        return tipo != null && "ADM".equals(tipo.toString());
    }
}