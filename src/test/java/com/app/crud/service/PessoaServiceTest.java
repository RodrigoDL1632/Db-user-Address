package com.app.crud.service;

import com.app.crud.model.Pessoa;
import com.app.crud.model.Endereco;
import com.app.crud.repository.PessoaRepository;
import com.app.crud.exception.ResourceNotFoundException;
import com.app.crud.exception.DuplicateCpfException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    private PessoaService pessoaService;

    @BeforeEach
    void setUp() {
        pessoaService = new PessoaService(pessoaRepository);
    }

    @Test
    void findAll_DeveRetornarPaginaDePessoas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Pessoa> pessoas = List.of(criarPessoa(), criarPessoa());
        Page<Pessoa> page = new PageImpl<>(pessoas);
        
        when(pessoaRepository.findAll(pageable)).thenReturn(page);

        Page<Pessoa> resultado = pessoaService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(pessoaRepository).findAll(pageable);
    }

    @Test
    void findById_QuandoPessoaExiste_DeveRetornarPessoa() {
        Long id = 1L;
        Pessoa pessoa = criarPessoa();
        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoa));

        Pessoa resultado = pessoaService.findById(id);

        assertNotNull(resultado);
        assertEquals(pessoa.getNome(), resultado.getNome());
        verify(pessoaRepository).findById(id);
    }

    @Test
    void findById_QuandoPessoaNaoExiste_DeveLancarException() {
        Long id = 1L;
        when(pessoaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pessoaService.findById(id));
        verify(pessoaRepository).findById(id);
    }

    @Test
    void save_QuandoCpfUnico_DeveSalvarPessoa() {
        Pessoa pessoa = criarPessoa();
        when(pessoaRepository.existsByCpf(pessoa.getCpf())).thenReturn(false);
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        Pessoa resultado = pessoaService.save(pessoa);

        assertNotNull(resultado);
        assertEquals(pessoa.getNome(), resultado.getNome());
        verify(pessoaRepository).existsByCpf(pessoa.getCpf());
        verify(pessoaRepository).save(pessoa);
    }

    @Test
    void save_QuandoCpfDuplicado_DeveLancarException() {
        Pessoa pessoa = criarPessoa();
        when(pessoaRepository.existsByCpf(pessoa.getCpf())).thenReturn(true);

        assertThrows(DuplicateCpfException.class, () -> pessoaService.save(pessoa));
        verify(pessoaRepository).existsByCpf(pessoa.getCpf());
        verify(pessoaRepository, never()).save(any());
    }

    @Test
    void update_QuandoPessoaExiste_DeveAtualizarPessoa() {
        Long id = 1L;
        Pessoa pessoaExistente = criarPessoa();
        Pessoa pessoaAtualizada = criarPessoa();
        pessoaAtualizada.setNome("Nome Atualizado");

        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoaExistente));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoaAtualizada);

        Pessoa resultado = pessoaService.update(id, pessoaAtualizada);

        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getNome());
        verify(pessoaRepository).findById(id);
        verify(pessoaRepository).save(any());
    }

    @Test
    void delete_QuandoPessoaExiste_DeveDeletarPessoa() {
        Long id = 1L;
        Pessoa pessoa = criarPessoa();
        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoa));

        pessoaService.delete(id);

        verify(pessoaRepository).findById(id);
        verify(pessoaRepository).delete(pessoa);
    }

    private Pessoa criarPessoa() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("Test Person");
        pessoa.setCpf("12345678901");
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        
        Endereco endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Test Street");
        endereco.setNumero("123");
        endereco.setBairro("Test District");
        endereco.setCidade("Test City");
        endereco.setEstado("TS");
        endereco.setCep("12345-678");
        endereco.setEnderecoPrincipal(true);
        endereco.setPessoa(pessoa);

        pessoa.setEnderecos(new ArrayList<>(List.of(endereco)));
        return pessoa;
    }
}