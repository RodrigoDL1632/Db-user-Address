package com.app.crud.controller;

import com.app.crud.model.Pessoa;
import com.app.crud.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PessoaController.class)
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PessoaService pessoaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarTodos_DeveRetornarPaginaDePessoas() throws Exception {
        List<Pessoa> pessoas = List.of(criarPessoa(), criarPessoa());
        Page<Pessoa> page = new PageImpl<>(pessoas);
        
        when(pessoaService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void buscarPorId_QuandoPessoaExiste_DeveRetornarPessoa() throws Exception {
        Pessoa pessoa = criarPessoa();
        when(pessoaService.findById(1L)).thenReturn(pessoa);

        mockMvc.perform(get("/api/pessoas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()));
    }

    @Test
    void criar_ComDadosValidos_DeveCriarPessoa() throws Exception {
        Pessoa pessoa = criarPessoa();
        when(pessoaService.save(any(Pessoa.class))).thenReturn(pessoa);

        mockMvc.perform(post("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()));
    }

    @Test
    void atualizar_ComDadosValidos_DeveAtualizarPessoa() throws Exception {
        Pessoa pessoa = criarPessoa();
        when(pessoaService.update(eq(1L), any(Pessoa.class))).thenReturn(pessoa);

        mockMvc.perform(put("/api/pessoas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pessoa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(pessoa.getNome()));
    }

    @Test
    void deletar_QuandoPessoaExiste_DeveDeletarPessoa() throws Exception {
        doNothing().when(pessoaService).delete(1L);

        mockMvc.perform(delete("/api/pessoas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private Pessoa criarPessoa() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("Test Person");
        pessoa.setCpf("12345678901");
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        return pessoa;
    }
}