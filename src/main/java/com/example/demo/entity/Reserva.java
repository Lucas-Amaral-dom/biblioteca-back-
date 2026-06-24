package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.StatusReserva;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reserva")
@EqualsAndHashCode(callSuper = false)
public class Reserva extends BaseEntity {

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Recurso Recurso;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusReserva status = StatusReserva.ATIVA;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "numero_pessoas")
    private Integer numeroPessoas;

    @Column(name = "checked_in")
    private Boolean checkedIn = false;

    @Column(name = "checked_in_em")
    private LocalDateTime checkedInEm;

    @Column(name = "checked_out")
    private Boolean checkedOut = false;

    @Column(name = "checked_out_em")
    private LocalDateTime checkedOutEm;

    @Column(name = "presenca_confirmada")
    private Boolean presencaConfirmada = false;

    @Column(name = "presenca_confirmada_em")
    private LocalDateTime presencaConfirmadaEm;

    @Column(name = "cancelado_por_admin")
    private Boolean canceladoPorAdmin = false;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "solicitante_tipo")
    private String solicitanteTipo;

    @Column(name = "aluno_id")
    private Long alunoId;

}
