package br.com.churrasquinho.controller;

import br.com.churrasquinho.entity.Usuario;
import br.com.churrasquinho.enums.TipoUsuario;
import br.com.churrasquinho.repository.UsuarioRepository;
import br.com.churrasquinho.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private HttpSession session;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario adm;

    @BeforeEach
    void setUp() {
        adm = new Usuario();
        adm.setId(1L);
        adm.setTipo(TipoUsuario.ADM);
        adm.setAtivo(true);
    }

    @Test
    void naoDevePermitirExcluirUltimoAdm() {
        // Configura a sessão para simular que o usuário logado é o ADM 1
        when(session.getAttribute("usuarioTipo")).thenReturn("ADM");
        when(session.getAttribute("usuarioId")).thenReturn(1L);

        // Simula que o banco só tem 1 ADM ativo
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adm));
        when(usuarioRepository.findAll()).thenReturn(List.of(adm));

        // Tenta excluir a si mesmo
        ResponseEntity<?> response = usuarioController.excluir(1L, session);

        // Validações
        assertEquals(400, response.getStatusCode().value(), "Deve retornar erro 400");
        assertTrue(response.getBody().toString().contains("último ADM ativo"), 
                "A mensagem deve avisar sobre o último ADM");
    }
}