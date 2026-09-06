package com.example.armazem.repository;

import com.example.armazem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Interface de acesso a dados dos usuarios

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
}