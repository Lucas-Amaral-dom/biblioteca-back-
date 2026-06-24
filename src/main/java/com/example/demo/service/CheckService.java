package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Reserva;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.StatusReserva;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class CheckService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckinRepository checkinRepository;

    public CheckinResponseDTO checkin(CheckinDTO dto){
        Reserva reserva = reservaRepository.findById(dto.getReservaId())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));

        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Você só pode fazer check-in nas suas próprias reservas.");
        }

        if (reserva.getStatus() != StatusReserva.ATIVA && reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw new RuntimeException("Não é possível fazer check-in em uma reserva que não esteja CONFIRMADA ou ATIVA.");
        }

        Checkin checkin = new Checkin();
        checkin.setReserva(reserva);
        checkin.setUsuario(usuario);

        if (dto.getFoto() != null) {
            Arquivo arquivo = arquivoService.upload(dto.getFoto());
            checkin.setFoto(arquivo);
        }

        Checkin checkinSalvo = checkinRepository.save(checkin);

        reserva.setCheckedIn(true);
        reserva.setCheckedInEm(java.time.LocalDateTime.now());
        reservaRepository.save(reserva);

        CheckinResponseDTO crd = new CheckinResponseDTO();
        crd.setReservaId(reserva.getId());
        crd.setRecurso(reserva.getRecurso().getNome());
        crd.setHorario(checkinSalvo.getCreatedAt());

        return crd;
    }
}
