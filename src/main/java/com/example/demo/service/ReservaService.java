package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.entity.Recurso;
import com.example.demo.entity.Reserva;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.enums.StatusRecurso;
import com.example.demo.enums.StatusReserva;
import com.example.demo.enums.TipoRecurso;
import com.example.demo.repository.RecursoRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class ReservaService extends BaseService<Reserva, ReservaDTO> {

    private final ReservaRepository reservaRepository;
    private final RecursoRepository RecursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public ReservaService(ReservaRepository repository, RecursoRepository RecursoRepository, UsuarioRepository usuarioRepository) {
        super(repository);
        this.reservaRepository = repository;
        this.RecursoRepository = RecursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public ReservaDTO toDto(Reserva entity) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(entity.getId());
        dto.setUsuarioId(entity.getUsuario().getId());
        dto.setUsuarioNome(entity.getUsuario().getNome());
        dto.setRecursoId(entity.getRecurso().getId());
        dto.setRecursoNome(entity.getRecurso().getNome());
        dto.setDataHoraInicio(entity.getDataHoraInicio());
        dto.setDataHoraFim(entity.getDataHoraFim());
        dto.setStatus(entity.getStatus());
        dto.setMotivo(entity.getMotivo());
        dto.setNumeroPessoas(entity.getNumeroPessoas());
        dto.setAlunoId(entity.getAlunoId());
        
        if (entity.getAlunoId() != null) {
            usuarioRepository.findById(entity.getAlunoId()).ifPresent(aluno -> {
                dto.setAlunoNome(aluno.getNome());
            });
        }
        
        dto.setCheckedIn(entity.getCheckedIn());
        dto.setCheckedOut(entity.getCheckedOut());
        dto.setPresencaConfirmada(entity.getPresencaConfirmada());
        dto.setPresencaConfirmadaEm(entity.getPresencaConfirmadaEm());
        dto.setConfirmadoEm(entity.getConfirmadoEm());
        
        return dto;
    }

    @Override
    public Reserva toEntity(ReservaDTO dto) {
        Reserva entity = new Reserva();
        entity.setId(dto.getId());
        entity.setDataHoraInicio(dto.getDataHoraInicio());
        entity.setDataHoraFim(dto.getDataHoraFim());
        entity.setMotivo(dto.getMotivo());
        entity.setNumeroPessoas(dto.getNumeroPessoas());
        entity.setAlunoId(dto.getAlunoId());
        return entity;
    }

    public ReservaDTO fazerReserva(ReservaDTO dto) {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (usuario.getNivelAcesso() == NivelAcesso.ADMIN && dto.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElse(usuario);
        }

        Recurso Recurso = RecursoRepository.findById(dto.getRecursoId())
                .orElseThrow(() -> new RuntimeException("Recurso não encontrado."));

        if (Recurso.getStatus() != StatusRecurso.DISPONIVEL) {
            throw new RuntimeException("Este recurso não está disponível para reserva.");
        }

        if (dto.getDataHoraInicio().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("A data de início deve ser no futuro.");
        }

        if (dto.getDataHoraFim().isBefore(dto.getDataHoraInicio())) {
            throw new RuntimeException("A data de fim deve ser posterior à data de início.");
        }

        long duracaoMinutos = java.time.Duration.between(dto.getDataHoraInicio(), dto.getDataHoraFim()).toMinutes();
        if (duracaoMinutos < 60) {
            throw new RuntimeException("A reserva deve ter duração mínima de 1 hora.");
        }

        if (duracaoMinutos > 120) {
            throw new RuntimeException("A reserva não pode ter duração maior que 2 horas.");
        }

        // Validate capacity constraints per resource
        List<StatusReserva> statusAtivos = Arrays.asList(StatusReserva.ATIVA, StatusReserva.PENDENTE, StatusReserva.CONFIRMADA, StatusReserva.BLOQUEADA);
        Long currentReservations = reservaRepository.countByRecursoAndHorario(Recurso.getId(), dto.getDataHoraInicio(), dto.getDataHoraFim(), statusAtivos);
        
        if (Recurso.getTipo() == TipoRecurso.COMPUTADOR && currentReservations >= 2) {
            throw new RuntimeException("O computador atingiu o limite de capacidade (2 pessoas) para este horário.");
        }
        if (Recurso.getTipo() == TipoRecurso.SALA_ESTUDO && currentReservations >= 5) {
            throw new RuntimeException("A sala atingiu o limite de capacidade (5 pessoas) para este horário.");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setRecurso(Recurso);
        reserva.setDataHoraInicio(dto.getDataHoraInicio());
        reserva.setDataHoraFim(dto.getDataHoraFim());
        reserva.setMotivo(dto.getMotivo());
        reserva.setNumeroPessoas(dto.getNumeroPessoas());
        reserva.setAlunoId(dto.getAlunoId());
        reserva.setSolicitanteTipo(usuario.getNivelAcesso().name());
        
        // Pendente for normal users, Confirmada for Admin
        if (usuario.getNivelAcesso() == NivelAcesso.ADMIN) {
            reserva.setStatus(StatusReserva.CONFIRMADA);
            reserva.setConfirmadoEm(LocalDateTime.now());
        } else {
            reserva.setStatus(StatusReserva.PENDENTE);
        }

        reserva = reservaRepository.save(reserva);
        return toDto(reserva);
    }

    public List<ReservaDTO> listarMinhasReservas() {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        return reservaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void cancelarReserva(Long id) {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Você só pode cancelar suas próprias reservas.");
        }

        // Rule: Can only cancel if PENDENTE or (CONFIRMADA AND <= 1h after confirmation)
        if (!isAdmin && reserva.getStatus() == StatusReserva.CONFIRMADA) {
            if (reserva.getConfirmadoEm() != null) {
                long horasDesdeConfirmacao = java.time.Duration.between(reserva.getConfirmadoEm(), LocalDateTime.now()).toHours();
                if (horasDesdeConfirmacao > 1) {
                    throw new RuntimeException("Não é possível cancelar: já se passou mais de 1 hora desde a confirmação.");
                }
            }
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        if (isAdmin) {
            reserva.setCanceladoPorAdmin(true);
        }
        reserva.setCanceladoEm(LocalDateTime.now());
        reservaRepository.save(reserva);
    }

    public void aprovarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setConfirmadoEm(LocalDateTime.now());
        reservaRepository.save(reserva);
    }

    public void recusarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));
        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setCanceladoPorAdmin(true);
        reserva.setCanceladoEm(LocalDateTime.now());
        reservaRepository.save(reserva);
    }
    
    public void fazerCheckin(Long id) {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Você só pode fazer check-in nas suas próprias reservas.");
        }

        if (reserva.getStatus() != StatusReserva.CONFIRMADA && reserva.getStatus() != StatusReserva.ATIVA) {
            throw new RuntimeException("A reserva não está confirmada.");
        }

        // Rule: Check-in only allowed <= 5 mins before start time
        long minAteInicio = java.time.Duration.between(LocalDateTime.now(), reserva.getDataHoraInicio()).toMinutes();
        if (minAteInicio > 5) {
            throw new RuntimeException("O check-in só pode ser feito até 5 minutos antes do início da reserva.");
        }

        reserva.setCheckedIn(true);
        reserva.setCheckedInEm(LocalDateTime.now());
        reservaRepository.save(reserva);
    }

    public void fazerCheckout(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));

        if (!reserva.getCheckedIn()) {
            throw new RuntimeException("Check-in ainda não realizado.");
        }

        reserva.setCheckedOut(true);
        reserva.setCheckedOutEm(LocalDateTime.now());
        reserva.setStatus(StatusReserva.CONCLUIDA);
        reservaRepository.save(reserva);
    }

    public ReservaDTO bloquearHorario(ReservaDTO dto) {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Recurso Recurso = RecursoRepository.findById(dto.getRecursoId())
                .orElseThrow(() -> new RuntimeException("Recurso não encontrado."));

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setRecurso(Recurso);
        reserva.setDataHoraInicio(dto.getDataHoraInicio());
        reserva.setDataHoraFim(dto.getDataHoraFim());
        reserva.setMotivo(dto.getMotivo() != null ? dto.getMotivo() : "Bloqueio de Manutenção/Admin");
        reserva.setStatus(StatusReserva.BLOQUEADA);
        reserva.setSolicitanteTipo(usuario.getNivelAcesso().name());
        
        reserva = reservaRepository.save(reserva);
        return toDto(reserva);
    }

    public List<ReservaDTO> listarTodas() {
        return reservaRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReservaDTO> listarPendentes() {
        return reservaRepository.findByStatus(StatusReserva.PENDENTE).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReservaDTO> listarConfirmadas() {
        return reservaRepository.findByStatus(StatusReserva.CONFIRMADA).stream().map(this::toDto).collect(Collectors.toList());
    }
    
    public List<ReservaDTO> buscarPorDataERecurso(Long recursoId, LocalDateTime inicio, LocalDateTime fim) {
        return reservaRepository.findByRecursoIdAndDataBetween(recursoId, inicio, fim).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void confirmarPresenca(Long id) {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Você só pode confirmar presença nas suas próprias reservas.");
        }

        if (reserva.getStatus() != StatusReserva.CONFIRMADA && reserva.getStatus() != StatusReserva.ATIVA) {
            throw new RuntimeException("A reserva não está confirmada.");
        }

        reserva.setPresencaConfirmada(true);
        reserva.setPresencaConfirmadaEm(LocalDateTime.now());
        reservaRepository.save(reserva);
    }
}
