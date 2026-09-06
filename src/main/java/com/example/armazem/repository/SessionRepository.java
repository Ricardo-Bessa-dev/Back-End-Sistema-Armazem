package com.example.armazem.repository;

import com.example.armazem.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Interface de acesso a dados das sessoes (tokens ativos)

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByToken(String token);
}