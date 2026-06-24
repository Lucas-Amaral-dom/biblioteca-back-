package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Reserva;
import com.example.demo.enums.StatusReserva;

@Repository
public interface ReservaRepository extends BaseRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    @Query("SELECT r FROM Reserva r WHERE r.Recurso.id = :RecursoId AND r.status = :status AND " +
           "((r.dataHoraInicio < :fim AND r.dataHoraFim > :inicio))")
    List<Reserva> findConflitos(@Param("RecursoId") Long RecursoId, 
                                @Param("inicio") LocalDateTime inicio, 
                                @Param("fim") LocalDateTime fim,
                                @Param("status") StatusReserva status);
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.Recurso.id = :RecursoId AND r.status IN :statusList AND " +
           "((r.dataHoraInicio < :fim AND r.dataHoraFim > :inicio))")
    Long countByRecursoAndHorario(@Param("RecursoId") Long RecursoId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim,
                                  @Param("statusList") List<StatusReserva> statusList);

    List<Reserva> findByStatus(StatusReserva status);

    @Query("SELECT r FROM Reserva r WHERE r.Recurso.id = :recursoId AND r.dataHoraInicio >= :inicio AND r.dataHoraInicio < :fim")
    List<Reserva> findByRecursoIdAndDataBetween(@Param("recursoId") Long recursoId, 
                                                @Param("inicio") LocalDateTime inicio, 
                                                @Param("fim") LocalDateTime fim);

    void deleteByUsuarioId(Long usuarioId);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.createdAt >= :inicio AND r.createdAt <= :fim")
    Long countByCreatedAtBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Busca reservas CANCELADAS ou RECUSADAS cujo cancelamento ocorreu há mais de X dias.
     * Usa canceladoEm se disponível, senão usa updatedAt como fallback.
     */
    @Query("SELECT r FROM Reserva r WHERE r.status IN ('CANCELADA', 'RECUSADA') " +
           "AND (r.canceladoEm IS NOT NULL AND r.canceladoEm < :limite " +
           "     OR r.canceladoEm IS NULL AND r.updatedAt < :limite)")
    List<Reserva> findReservasParaLimpeza(@Param("limite") LocalDateTime limite);
}
