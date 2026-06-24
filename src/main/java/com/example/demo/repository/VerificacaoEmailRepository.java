package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.VerificacaoEmail;

@Repository
public interface VerificacaoEmailRepository extends JpaRepository<VerificacaoEmail, Long> {
    Optional<VerificacaoEmail> findTopByEmailOrderByCriadoEmDesc(String email);
    void deleteByEmail(String email);
}
