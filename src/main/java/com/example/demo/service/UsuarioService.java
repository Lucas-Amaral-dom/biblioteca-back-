package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository){
        super(repository);
        this.usuarioRepository = repository;
    }

    public Optional<Usuario> findByCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> findAllAtivos() {
        return usuarioRepository.findAllAtivos();
    }

    public Usuario salvarComSenhaEncriptada(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Autowired
    private com.example.demo.repository.CheckinRepository checkinRepository;

    @Autowired
    private com.example.demo.repository.ReservaRepository reservaRepository;

    public void deletarUsuario(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            u.setAtivo(false);
            usuarioRepository.save(u);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void cancelarMinhaConta(String senha) {
        String cpf = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta.");
        }

        // Deletar dependências (cascade delete para LGPD)
        checkinRepository.deleteByUsuarioId(usuario.getId());
        reservaRepository.deleteByUsuarioId(usuario.getId());
        
        usuarioRepository.delete(usuario);
    }
}
