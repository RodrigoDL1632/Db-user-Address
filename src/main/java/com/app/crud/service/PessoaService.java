package com.app.crud.service;

import com.app.crud.model.Pessoa;
import com.app.crud.repository.PessoaRepository;
import com.app.crud.exception.ResourceNotFoundException;
import com.app.crud.exception.DuplicateCpfException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {
    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public Page<Pessoa> findAll(Pageable pageable) {
        return pessoaRepository.findAll(pageable);
    }

    public Pessoa findById(Long id) {
        return pessoaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));
    }

    @Transactional
    public Pessoa save(Pessoa pessoa) {
        if (pessoaRepository.existsByCpf(pessoa.getCpf())) {
            throw new DuplicateCpfException("CPF já cadastrado");
        }
        pessoa.getEnderecos().forEach(endereco -> endereco.setPessoa(pessoa));
        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public Pessoa update(Long id, Pessoa pessoaAtualizada) {
        Pessoa pessoa = findById(id);
        
        if (!pessoa.getCpf().equals(pessoaAtualizada.getCpf()) && 
            pessoaRepository.existsByCpf(pessoaAtualizada.getCpf())) {
            throw new DuplicateCpfException("CPF já cadastrado");
        }

        pessoa.setNome(pessoaAtualizada.getNome());
        pessoa.setCpf(pessoaAtualizada.getCpf());
        pessoa.setDataNascimento(pessoaAtualizada.getDataNascimento());
        
        pessoa.getEnderecos().clear();
        pessoaAtualizada.getEnderecos().forEach(endereco -> {
            endereco.setPessoa(pessoa);
            pessoa.getEnderecos().add(endereco);
        });

        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void delete(Long id) {
        Pessoa pessoa = findById(id);
        pessoaRepository.delete(pessoa);
    }
}

