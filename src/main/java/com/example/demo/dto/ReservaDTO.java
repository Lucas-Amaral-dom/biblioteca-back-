package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.StatusReserva;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long usuarioId;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String usuarioNome;

    @NotNull(message = "O recurso (Recurso) deve ser informado.")
    private Long RecursoId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String RecursoNome;

    @NotNull(message = "A data e hora de início devem ser informadas.")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "A data e hora de fim devem ser informadas.")
    private LocalDateTime dataHoraFim;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private StatusReserva status;

    private String motivo;
    
    private Integer numeroPessoas;
    
    private Long alunoId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String alunoNome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean checkedIn;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean checkedOut;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean presencaConfirmada;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime presencaConfirmadaEm;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime confirmadoEm;

}
