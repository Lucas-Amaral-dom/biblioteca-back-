package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.RecursoDTO;
import com.example.demo.entity.Recurso;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.RecursoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest
@ActiveProfiles("test")
public class RecursoControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwt;

    private String token;

    @Autowired
    private RecursoRepository pr;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        this.token = jwt.generateToken("tantofazcomotantofez@admin.com", 
        NivelAcesso.ADMIN.toString());
    }

    @Test
    @DisplayName("Deve deletar Recurso pelo ID")
    void deletarId() throws Exception {
        Recurso Recurso = new Recurso();
        Recurso.setNome("Recurso Para DELETARR por ID");
        Recurso.setDescricao("Recurso buscavel");

        Recurso = pr.save(Recurso);

        mockMvc.perform(delete("/Recursos/" + Recurso.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Recurso = pr.findById(Recurso.getId()).orElseThrow();

        assertFalse(Recurso.isAtivo());
    }

    @Test
    @DisplayName("Deve buscar Recurso pelo ID")
    void buscarPorId() throws Exception {
        Recurso Recurso = new Recurso();
        Recurso.setNome("Recurso Para Buscar por ID");
        Recurso.setDescricao("Recurso buscavel");

        Recurso = pr.save(Recurso);

        mockMvc.perform(get("/Recursos/" + Recurso.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nome")
                                .value("Recurso Para Buscar por ID"));

    }

    @Test
    @DisplayName("Deve criar um Recurso com sucesso")
    void criarRecurso() throws Exception {
        RecursoDTO RecursoDTO = new RecursoDTO();

        RecursoDTO.setNome("Recurso 12");
        RecursoDTO.setDescricao("Descrição do Recurso 12");

        String json = objectMapper.writeValueAsString(RecursoDTO);

        mockMvc.perform(
                post("/Recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Recurso 12"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve listar todos os registros")
    void listarRecursos() throws Exception {
        mockMvc.perform(get("/Recursos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve dar badrequest ao criar Recurso")
    void criarBadRequest() throws Exception {
        mockMvc.perform(post("/Recursos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

    }

}
