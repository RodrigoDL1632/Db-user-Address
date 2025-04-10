package com.app.crud.e2e;

import com.app.crud.model.Pessoa;
import com.app.crud.model.Endereco;
import com.app.crud.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class PessoaE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        pessoaRepository.deleteAll();
    }

    @Test
    void fluxoCompletoComEnderecos() throws Exception {
        // Criar pessoa com endereços
        Pessoa pessoa = criarPessoaComEnderecos();
        String pessoaJson = objectMapper.writeValueAsString(pessoa);

        String responseContent = mockMvc.perform(post("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pessoaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()))
                .andExpect(jsonPath("$.enderecos[0].rua").value(pessoa.getEnderecos().get(0).getRua()))
                .andReturn().getResponse().getContentAsString();

        Pessoa pessoaCriada = objectMapper.readValue(responseContent, Pessoa.class);

        // Atualizar endereço
        pessoa.getEnderecos().get(0).setRua("Nova Rua");
        mockMvc.perform(put("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enderecos[0].rua").value("Nova Rua"));

        // Adicionar novo endereço
        Endereco novoEndereco = criarEndereco();
        novoEndereco.setRua("Rua Adicional");
        pessoa.getEnderecos().add(novoEndereco);

        mockMvc.perform(put("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enderecos.length()").value(2));

        // Verificar cálculo de idade
        mockMvc.perform(get("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idade").isNumber());
    }

    private Pessoa criarPessoaComEnderecos() {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("Test Person");
        pessoa.setCpf("12345678901");
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        
        Endereco endereco = criarEndereco();
        pessoa.setEnderecos(new ArrayList<>(List.of(endereco)));
        
        return pessoa;
    }

    private Endereco criarEndereco() {
        Endereco endereco = new Endereco();
        endereco.setRua("Test Street");
        endereco.setNumero("123");
        endereco.setBairro("Test District");
        endereco.setCidade("Test City");
        endereco.setEstado("TS");
        endereco.setCep("12345-678");
        endereco.setEnderecoPrincipal(true);
        return endereco;
    }
}