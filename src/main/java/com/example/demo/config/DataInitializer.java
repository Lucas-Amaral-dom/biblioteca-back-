package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Recurso;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.enums.StatusRecurso;
import com.example.demo.enums.TipoRecurso;
import com.example.demo.repository.RecursoRepository;
import com.example.demo.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository repository, RecursoRepository recursoRepository){
        return args -> {
            if(repository.count() <= 0){
                Usuario admin = new Usuario();
                admin.setNome("Administrador SENAI");
                admin.setCpf("11111111111");
                admin.setEmail("admin@senai.com.br");
                admin.setNivelAcesso(NivelAcesso.ADMIN);
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setTipoUsuario("administrador");
                admin.setCategoria("admin");
                admin.setStatus("ativo");
                admin.setAtivo(true);

                repository.save(admin);

                System.out.println("========================================");
                System.out.println("Usuário ADMIN criado com sucesso!");
                System.out.println("========================================");
            }

            if (recursoRepository.count() <= 0) {
                for (int i = 1; i <= 4; i++) {
                    Recurso sala = new Recurso();
                    sala.setNome("Sala de Estudos " + i);
                    sala.setCodigo(String.format("S-%03d", i));
                    sala.setCapacidade(5);
                    sala.setStatus(StatusRecurso.DISPONIVEL);
                    sala.setTipo(TipoRecurso.SALA_ESTUDO);
                    recursoRepository.save(sala);
                }

                for (int i = 1; i <= 12; i++) {
                    Recurso pc = new Recurso();
                    pc.setNome(String.format("Computador %02d", i));
                    pc.setCodigo(String.format("PC-%03d", i));
                    pc.setMesa("Mesa " + (int) Math.ceil((double) i / 3));
                    pc.setCapacidade(2);
                    pc.setStatus(StatusRecurso.DISPONIVEL);
                    pc.setTipo(TipoRecurso.COMPUTADOR);
                    recursoRepository.save(pc);
                }

                System.out.println("========================================");
                System.out.println("Recursos (Salas e Computadores) criados!");
                System.out.println("========================================");
            }
        };
    }
}

