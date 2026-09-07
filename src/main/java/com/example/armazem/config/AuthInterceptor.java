package com.example.armazem.config;

import com.example.armazem.exception.UnauthorizedException;
import com.example.armazem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Roda antes de cada requisicao: exige um token valido no header Authorization

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(HEADER);

        if (header == null || !header.startsWith(PREFIX)) {
            throw new UnauthorizedException("Token ausente ou malformado");
        }

        String token = header.substring(PREFIX.length());

        if (!authService.isValidToken(token)) {
            throw new UnauthorizedException("Token invalido ou expirado");
        }

        return true;
    }
}