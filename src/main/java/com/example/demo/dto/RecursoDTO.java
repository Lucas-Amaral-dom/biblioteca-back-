package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.enums.StatusRecurso;
import com.example.demo.enums.TipoRecurso;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecursoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome deve ser preenchido.")
    private String nome;

    private String descricao;

    private TipoRecurso tipo;

    private Integer capacidade;

    private StatusRecurso status;

    private String codigo;
    private String laboratorio;
    private String mesa;
}
