package com.app.crud.repository;

import com.app.crud.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    boolean existsByCpf(String cpf);
    Optional<Pessoa> findByCpf(String cpf);
}