package com.example.armazem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ArmazemApplication {

	public static void main(String[] args) {

		SpringApplication.run(ArmazemApplication.class, args);

	}
	@Bean // Método devolve uma instância que será guardada em contêiner e pode ser acessada pelo construtor de qualquer classe (nesse caso AuthService e UserService)
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
}
