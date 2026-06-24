package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Public;
import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AuthDTO;
import com.example.demo.dto.CadastroDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import com.example.demo.service.EmailService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/login")
    @Public
    public ResponseEntity<?> login(@RequestBody @Valid AuthDTO dto) {
        try {
            String cpfLimpo = dto.getCpf().replaceAll("[^\\d]", "");
            String senha = dto.getSenha();

            Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfLimpo);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                    "erro", "CPF ou senha incorretos"
                ));
            }

            Usuario usuario = usuarioOpt.get();

            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                return ResponseEntity.status(401).body(Map.of(
                    "erro", "CPF ou senha incorretos"
                ));
            }

            String role = usuario.getNivelAcesso() != null ? usuario.getNivelAcesso().name() : NivelAcesso.ALUNO.name();
            String token = jwtUtil.generateToken(usuario.getCpf(), role);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setId(usuario.getId());
            response.setNome(usuario.getNome());
            response.setCpf(usuario.getCpf());
            response.setTipoUsuario(role);
            response.setCategoria(usuario.getCategoria());
            response.setDadosAdicionais(usuario.getDadosAdicionais());
            response.setDataCadastro(usuario.getCreatedAt() != null ? usuario.getCreatedAt().toString() : "");
            response.setToken(token);
            response.setTipo(role);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "erro", "Erro ao processar login"
            ));
        }
    }

    @PostMapping("/cadastro")
    @Public
    public ResponseEntity<?> cadastro(@RequestBody @Valid CadastroDTO dto) {
        try {
            String cpfLimpo = dto.getCpf().replaceAll("[^\\d]", "");

            // Validar se CPF já existe
            if (usuarioRepository.findByCpf(cpfLimpo).isPresent()) {
                return ResponseEntity.status(400).body(Map.of(
                    "erro", "CPF já cadastrado"
                ));
            }

            // Validar se email já existe
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of(
                    "erro", "Email já cadastrado"
                ));
            }

            // Validar código de verificação
            if (dto.getCodigoVerificacao() == null || dto.getCodigoVerificacao().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                    "erro", "Código de verificação é obrigatório"
                ));
            }

            if (!emailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())) {
                return ResponseEntity.status(400).body(Map.of(
                    "erro", "Código de verificação inválido ou expirado"
                ));
            }

            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setCpf(cpfLimpo);
            usuario.setEmail(dto.getEmail());
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
            usuario.setTipoUsuario(dto.getNivelAcesso() != null ? dto.getNivelAcesso() : "ALUNO");
            usuario.setCategoria(dto.getCategoria());
            
            try {
                NivelAcesso nivel = dto.getNivelAcesso() != null ? NivelAcesso.valueOf(dto.getNivelAcesso().toUpperCase()) : NivelAcesso.ALUNO;
                usuario.setNivelAcesso(nivel);
            } catch (IllegalArgumentException e) {
                usuario.setNivelAcesso(NivelAcesso.ALUNO);
            }
            usuario.setStatus("ativo");
            usuario.setAtivo(true);
            usuario.setDadosAdicionais(dto.getDadosAdicionais());

            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            emailService.limparCodigo(dto.getEmail());

            return ResponseEntity.ok(Map.of(
                "id", usuarioSalvo.getId(),
                "nome", usuarioSalvo.getNome(),
                "cpf", usuarioSalvo.getCpf(),
                "email", usuarioSalvo.getEmail(),
                "mensagem", "Cadastro realizado com sucesso"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "erro", "Erro ao realizar cadastro: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/solicitar-codigo")
    @Public
    public ResponseEntity<?> solicitarCodigo(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("erro", "Email é obrigatório"));
            }
            if (usuarioRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("erro", "Email já cadastrado"));
            }
            emailService.gerarEnviarCodigo(email);
            return ResponseEntity.ok(Map.of("mensagem", "Código de verificação enviado para o email"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao enviar código de verificação: " + e.getMessage()));
        }
    }

    @PostMapping("/recuperar-senha")
    @Public
    public ResponseEntity<?> recuperarSenha(@RequestBody @Valid com.example.demo.dto.RecuperarSenhaDTO dto) {
        try {
            String cpfLimpo = dto.getCpf().replaceAll("[^\\d]", "");
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfLimpo);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("erro", "Usuário não encontrado."));
            }

            Usuario usuario = usuarioOpt.get();
            if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
                return ResponseEntity.status(400).body(Map.of("erro", "O e-mail informado não corresponde ao CPF."));
            }

            usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso!"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao recuperar senha: " + e.getMessage()));
        }
    }

    @GetMapping("/ping")
    @Public
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

}
