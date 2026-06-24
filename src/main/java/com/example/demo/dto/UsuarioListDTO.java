package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO seguro para listar usuários — nunca expõe o hash de senha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioListDTO {

    private Long id;
    private String nome;
    private String email;
    private String nivelAcesso;
    private String categoria;
    private String status;
    private String createdAt;
}
