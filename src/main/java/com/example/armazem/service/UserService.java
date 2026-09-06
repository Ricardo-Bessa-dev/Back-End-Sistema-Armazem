package com.example.armazem.service;

import com.example.armazem.dto.CreateUserRequest;
import com.example.armazem.entity.User;
import com.example.armazem.exception.ConflictException;
import com.example.armazem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Regras de negócio do cadastro de usuários

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void create(CreateUserRequest request){
        if(userRepository.existsByLogin(request.login())){
            throw new ConflictException("Login já cadastrado: " + request.login());
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.login(), passwordHash);
        userRepository.save(user);
    }
}