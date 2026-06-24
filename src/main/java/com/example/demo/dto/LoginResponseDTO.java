package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String tipoUsuario;
    private String categoria;
    private String dadosAdicionais;
    private String dataCadastro;
    private String token;
    private String tipo;

}
