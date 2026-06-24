package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.example.demo.enums.StatusRecurso;
import com.example.demo.enums.TipoRecurso;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Recurso")
@EqualsAndHashCode(callSuper = false)
public class Recurso extends BaseEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "laboratorio")
    private String laboratorio;

    @Column(name = "mesa")
    private String mesa;

    @Column(name = "descricao", nullable = true)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRecurso tipo = TipoRecurso.COMPUTADOR;

    @Column(name = "capacidade")
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusRecurso status = StatusRecurso.DISPONIVEL;

}
