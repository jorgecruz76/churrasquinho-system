package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.enums.TipoUsuario;
import br.com.churrasquinho.repository.ClienteRepository;
import br.com.churrasquinho.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvar(Usuario usuario) {

        if (usuario.getSenha() != null &&
                !usuario.getSenha().startsWith("$2a$") &&
                !usuario.getSenha().startsWith("$2b$") &&
                !usuario.getSenha().startsWith("$2y$")) {

            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario autenticar(String email, String senha) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "E-mail ou senha inválidos."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException(
                    "Usuário inativo.");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos.");
        }

        return usuario;
    }

    @Transactional
    public Usuario cadastrarCliente(String nome, String email, String senha) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo 6 caracteres.");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(senha); // será criptografada no salvar()
        usuario.setTipo(TipoUsuario.CLIENTE);
        usuario.setAtivo(true);

        usuario = salvar(usuario);

        Cliente cliente = new Cliente();
        cliente.setNome(nome.trim());
        cliente.setEmail(emailNormalizado);
        cliente.setAtivo(true);
        cliente.setUsuario(usuario);

        clienteRepository.save(cliente);

        return usuario;
    }
}