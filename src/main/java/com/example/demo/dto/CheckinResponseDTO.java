package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResponseDTO {

    private Long reservaId;
    
    private String recurso;
    
    private LocalDateTime horario;

}
