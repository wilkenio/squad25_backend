package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Rota para listar todos os usuários
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Você pode adicionar outras rotas aqui, como getUserById, deleteUser, updateUser, etc.
}
