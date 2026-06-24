package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Reserva;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.ReservaRepository;

/**
 * Serviço de limpeza automática de histórico de reservas.
 *
 * Regra de negócio:
 *   - Reservas com status CANCELADA ou RECUSADA são excluídas permanentemente
 *     após 7 dias do cancelamento.
 *   - O job roda todos os dias à meia-noite (00:00).
 */
@Service
public class LimpezaService {

    private static final Logger log = LoggerFactory.getLogger(LimpezaService.class);

    /** Dias de retenção do histórico de reservas canceladas/recusadas */
    private static final int DIAS_RETENCAO = 7;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    /**
     * Executa a limpeza do histórico todos os dias à meia-noite.
     * cron: segundo minuto hora dia mês dia-semana
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void limparHistoricoReservasCanceladas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_RETENCAO);

        log.info("[Limpeza] Iniciando limpeza de reservas canceladas/recusadas anteriores a {}", limite);

        List<Reserva> reservasAnteriores = reservaRepository.findReservasParaLimpeza(limite);

        if (reservasAnteriores.isEmpty()) {
            log.info("[Limpeza] Nenhuma reserva para excluir.");
            return;
        }

        int total = 0;
        for (Reserva reserva : reservasAnteriores) {
            try {
                // Remove check-ins relacionados para evitar violação de FK
                checkinRepository.deleteByReservaId(reserva.getId());

                reservaRepository.delete(reserva);
                total++;
                log.debug("[Limpeza] Reserva #{} excluída (cancelada em {})",
                          reserva.getId(), reserva.getCanceladoEm());
            } catch (Exception e) {
                log.error("[Limpeza] Erro ao excluir reserva #{}: {}", reserva.getId(), e.getMessage());
            }
        }

        log.info("[Limpeza] Concluída. {} reserva(s) removida(s) do histórico.", total);
    }
}
