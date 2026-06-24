package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.CadastroDTO;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.dto.UsuarioListDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService service){
        this.usuarioService = service;
    }

    @GetMapping("/ativos")
    public ResponseEntity<?> listarAtivos() {
        try {
            List<UsuarioListDTO> usuarios = usuarioService.findAllAtivos()
                .stream()
                .map(u -> new UsuarioListDTO(
                    u.getId(),
                    u.getNome(),
                    u.getEmail(),
                    u.getNivelAcesso() != null ? u.getNivelAcesso().name() : "ALUNO",
                    u.getCategoria(),
                    u.getStatus(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : null
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao listar usuários"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obterUsuario(@PathVariable Long id) {
        try {
            Optional<Usuario> usuario = usuarioService.findById(id);
            if (usuario.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("erro", "Usuário não encontrado"));
            }
            return ResponseEntity.ok(usuario.get());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao obter usuário"));
        }
    }

    @PostMapping
    @Admin
    public ResponseEntity<?> criarUsuario(@RequestBody CadastroDTO dto) {
        try {
            String cpfLimpo = dto.getCpf().replaceAll("[^\\d]", "");

            if (usuarioService.findByCpf(cpfLimpo).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("erro", "CPF já cadastrado"));
            }

            if (usuarioService.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("erro", "Email já cadastrado"));
            }

            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setCpf(cpfLimpo);
            usuario.setEmail(dto.getEmail());
            usuario.setSenha(dto.getSenha());
            usuario.setTipoUsuario("aluno");
            usuario.setCategoria(dto.getCategoria());
            usuario.setNivelAcesso(NivelAcesso.PADRAO);
            usuario.setStatus("ativo");
            usuario.setAtivo(true);
            usuario.setDadosAdicionais(dto.getDadosAdicionais());

            Usuario usuarioSalvo = usuarioService.salvarComSenhaEncriptada(usuario);

            return ResponseEntity.ok(Map.of(
                "id", usuarioSalvo.getId(),
                "nome", usuarioSalvo.getNome(),
                "cpf", usuarioSalvo.getCpf(),
                "mensagem", "Usuário criado com sucesso"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao criar usuário"));
        }
    }

    @DeleteMapping("/{id}")
    @Admin
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok(Map.of("mensagem", "Usuário desativado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao desativar usuário"));
        }
    }

    @DeleteMapping("/{id}/hard")
    @Admin
    public ResponseEntity<?> excluirUsuarioFisicamente(@PathVariable Long id) {
        try {
            usuarioService.delete(id);
            return ResponseEntity.ok(Map.of("mensagem", "Usuário excluído permanentemente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao excluir usuário"));
        }
    }

    @PutMapping("/{id}")
    @Admin
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody CadastroDTO dto) {
        try {
            Optional<Usuario> opt = usuarioService.findById(id);
            if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("erro", "Usuário não encontrado"));
            
            Usuario usuario = opt.get();

            // PROTEÇÃO: nunca sobrescrever o nivelAcesso (role) do usuário via PUT
            // O role permanece sempre o que está salvo no banco de dados
            NivelAcesso nivelAcessoOriginal = usuario.getNivelAcesso();

            if (dto.getNome() != null && !dto.getNome().isEmpty()) usuario.setNome(dto.getNome());
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) usuario.setEmail(dto.getEmail());
            if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) usuario.setCategoria(dto.getCategoria());

            // Restaurar role original — nunca permite alteração de nivelAcesso via este endpoint
            usuario.setNivelAcesso(nivelAcessoOriginal);

            if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
                usuario.setSenha(dto.getSenha());
                usuarioService.salvarComSenhaEncriptada(usuario);
            } else {
                usuarioService.salvarUsuario(usuario);
            }
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Usuário atualizado com sucesso",
                "id", usuario.getId(),
                "role", nivelAcessoOriginal != null ? nivelAcessoOriginal.name() : "GUARDA_VIDAS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao atualizar usuário"));
        }
    }

    @PostMapping("/minha-conta")
    public ResponseEntity<?> cancelarMinhaConta(@RequestBody Map<String, String> body) {
        try {
            String senha = body.get("senha");
            usuarioService.cancelarMinhaConta(senha);
            return ResponseEntity.ok(Map.of("mensagem", "Conta excluída com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("erro", e.getMessage()));
        }
    }

}
