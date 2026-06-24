package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Usuario;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = TRUE")
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf AND u.ativo = TRUE")
    Optional<Usuario> findByCpf(String cpf);

    @Query("SELECT u FROM Usuario u WHERE u.ativo = TRUE ORDER BY u.nome")
    List<Usuario> findAllAtivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.createdAt >= :inicio AND u.createdAt <= :fim")
    Long countByCreatedAtBetween(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio, @org.springframework.data.repository.query.Param("fim") java.time.LocalDateTime fim);

}
