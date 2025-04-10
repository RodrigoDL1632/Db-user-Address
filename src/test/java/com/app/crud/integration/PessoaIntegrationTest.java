package com.app.crud.integration;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.app.crud.model.Pessoa;
import com.app.crud.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PessoaIntegrationTest {

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
    void fluxoCompletoCRUD() throws Exception {
        // Criar pessoa
        Pessoa pessoa = criarPessoa();
        String pessoaJson = objectMapper.writeValueAsString(pessoa);

        String responseContent = mockMvc.perform(post("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pessoaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()))
                .andReturn().getResponse().getContentAsString();

        Pessoa pessoaCriada = objectMapper.readValue(responseContent, Pessoa.class);

        // Buscar pessoa
        mockMvc.perform(get("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()));

        // Atualizar pessoa
        pessoa.setNome("Nome Atualizado");
        mockMvc.perform(put("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));

        // Deletar pessoa
        mockMvc.perform(delete("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar se foi deletado
        mockMvc.perform(get("/api/pessoas/" + pessoaCriada.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarPessoa_ComCPFDuplicado_DeveRetornarErro() throws Exception {
        Pessoa pessoa1 = criarPessoa();
        pessoaRepository.save(pessoa1);

        Pessoa pessoa2 = criarPessoa();
        mockMvc.perform(post("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa2)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF já cadastrado"));
    }

    private Pessoa criarPessoa() {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("Test Person");
        pessoa.setCpf("12345678901");
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        return pessoa;
    }
}
