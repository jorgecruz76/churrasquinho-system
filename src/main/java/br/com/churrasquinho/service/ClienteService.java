package br.com.churrasquinho.service;

import br.com.churrasquinho.entity.Cliente;
import br.com.churrasquinho.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvar(Cliente cliente) {

        if (cliente.getId() != null) {

            Cliente clienteExistente = clienteRepository.findById(cliente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

            cliente.setDataCadastro(clienteExistente.getDataCadastro());
        }

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public void excluir(Long id) {
        clienteRepository.deleteById(id);
    }
}