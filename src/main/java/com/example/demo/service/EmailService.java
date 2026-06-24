package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.VerificacaoEmail;
import com.example.demo.repository.VerificacaoEmailRepository;

import jakarta.transaction.Transactional;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private VerificacaoEmailRepository verificacaoRepository;

    @Transactional
    public void gerarEnviarCodigo(String email) {
        // Excluir código anterior se existir
        verificacaoRepository.deleteByEmail(email);

        // Gerar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        VerificacaoEmail ve = new VerificacaoEmail();
        ve.setEmail(email);
        ve.setCodigo(codigo);
        ve.setExpiraEm(LocalDateTime.now().plusMinutes(15));
        verificacaoRepository.save(ve);

        // Tentar enviar email
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Código de Verificação - Espaço do Estudante");
                message.setText("Seu código de verificação é: " + codigo + "\nEste código expira em 15 minutos.");
                mailSender.send(message);
                System.out.println("[EMAIL ENVIADO] Código para " + email + ": " + codigo);
            } else {
                System.out.println("[MOCK EMAIL] JavaMailSender não configurado. Código para " + email + ": " + codigo);
            }
        } catch (Exception e) {
            System.err.println("[ERRO EMAIL] Não foi possível enviar o email para " + email + ": " + e.getMessage());
            System.out.println("[FALLBACK MOCK] Código para " + email + ": " + codigo);
        }
    }

    public boolean validarCodigo(String email, String codigo) {
        Optional<VerificacaoEmail> opt = verificacaoRepository.findTopByEmailOrderByCriadoEmDesc(email);
        if (opt.isPresent()) {
            VerificacaoEmail ve = opt.get();
            if (ve.getCodigo().equals(codigo) && ve.getExpiraEm().isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void limparCodigo(String email) {
        verificacaoRepository.deleteByEmail(email);
    }
}
