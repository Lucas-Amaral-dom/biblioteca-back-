package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.UsuarioRepository;

import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@Admin
public class RelatorioController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/ocupacao")
    public ResponseEntity<?> getOcupacao() {
        long totalReservas = reservaRepository.count();
        long usuariosAtivos = usuarioRepository.count();

        java.time.LocalDateTime inicioMes = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        java.time.LocalDateTime fimMes = java.time.LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        long reservasNoMes = reservaRepository.countByCreatedAtBetween(inicioMes, fimMes);
        long usuariosNoMes = usuarioRepository.countByCreatedAtBetween(inicioMes, fimMes);

        return ResponseEntity.ok(Map.of(
            "totalReservas", totalReservas,
            "usuariosAtivos", usuariosAtivos,
            "reservasNoMes", reservasNoMes,
            "usuariosNoMes", usuariosNoMes
        ));
    }
}
