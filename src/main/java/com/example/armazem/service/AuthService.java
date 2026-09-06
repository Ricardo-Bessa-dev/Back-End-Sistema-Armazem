package com.example.armazem.service;

import com.example.armazem.dto.LoginRequest;
import com.example.armazem.dto.LoginResponse;
import com.example.armazem.entity.Session;
import com.example.armazem.entity.User;
import com.example.armazem.exception.UnauthorizedException;
import com.example.armazem.repository.SessionRepository;
import com.example.armazem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

// Regras de autenticacao: conferir credenciais, emitir e validar tokens

@Service
public class AuthService{
    private static final int TOKEN_BYTES = 32;
    private static final int SESSION_DURATION_HOURS = 8;

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new UnauthorizedException("Login ou senha inválidos"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new UnauthorizedException("Login ou senha inválidos");
        }

        Session session = new Session(
                user,
                generateToken(),
                LocalDateTime.now().plusHours(SESSION_DURATION_HOURS)
        );
        sessionRepository.save(session);

        return new LoginResponse(session.getToken());
    }

    public boolean isValidToken(String token) {
        return sessionRepository.findByToken(token)
                .filter(session -> !session.isExpired())
                .isPresent();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}