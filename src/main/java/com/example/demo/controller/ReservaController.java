package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.service.ReservaService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    public ResponseEntity<?> criarReserva(@RequestBody @Valid ReservaDTO dto) {
        try {
            ReservaDTO reserva = reservaService.fazerReserva(dto);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<ReservaDTO>> listarMinhasReservas() {
        return ResponseEntity.ok(reservaService.listarMinhasReservas());
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {
        try {
            reservaService.cancelarReserva(id);
            return ResponseEntity.ok(Map.of("mensagem", "Reserva cancelada com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<List<ReservaDTO>> listarTodas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<ReservaDTO>> listarPendentes() {
        return ResponseEntity.ok(reservaService.listarPendentes());
    }

    @GetMapping("/confirmadas")
    public ResponseEntity<List<ReservaDTO>> listarConfirmadas() {
        return ResponseEntity.ok(reservaService.listarConfirmadas());
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovarReserva(@PathVariable Long id) {
        try {
            reservaService.aprovarReserva(id);
            return ResponseEntity.ok(Map.of("mensagem", "Reserva aprovada com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<?> recusarReserva(@PathVariable Long id) {
        try {
            reservaService.recusarReserva(id);
            return ResponseEntity.ok(Map.of("mensagem", "Reserva recusada com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> fazerCheckin(@PathVariable Long id) {
        try {
            reservaService.fazerCheckin(id);
            return ResponseEntity.ok(Map.of("mensagem", "Check-in realizado com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> fazerCheckout(@PathVariable Long id) {
        try {
            reservaService.fazerCheckout(id);
            return ResponseEntity.ok(Map.of("mensagem", "Check-out realizado com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/bloquear")
    public ResponseEntity<?> bloquearHorario(@RequestBody @Valid ReservaDTO dto) {
        try {
            ReservaDTO reserva = reservaService.bloquearHorario(dto);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/por-data")
    public ResponseEntity<List<ReservaDTO>> buscarPorData(
            @org.springframework.web.bind.annotation.RequestParam Long recursoId,
            @org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate data) {
        return ResponseEntity.ok(reservaService.buscarPorDataERecurso(recursoId, data.atStartOfDay(), data.plusDays(1).atStartOfDay()));
    }

    @PostMapping("/{id}/confirmar-presenca")
    public ResponseEntity<?> confirmarPresenca(@PathVariable Long id) {
        try {
            reservaService.confirmarPresenca(id);
            return ResponseEntity.ok(Map.of("mensagem", "Presença confirmada com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
