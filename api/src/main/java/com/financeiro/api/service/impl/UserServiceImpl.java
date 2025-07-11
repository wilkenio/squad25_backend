package com.financeiro.api.service.impl;

import com.financeiro.api.domain.User;
import com.financeiro.api.dto.userDTO.UserRequestDTO;
import com.financeiro.api.dto.userDTO.UserResponseDTO;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.UserService;
import com.financeiro.api.service.initializer.DefaultInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DefaultInitializer defaultInitializer;

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setStatus(Status.SIM);
        user.setCreatedAt(LocalDateTime.now());

        User saved = repository.save(user);
        defaultInitializer.createDefaultAccountAndCategoriesForUser(saved);
        return toDTO(saved);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(
                        () -> new UserNotFoundException()
                );
    }

    @Override
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User user = repository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        user.setName(dto.name());
        user.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        return toDTO(repository.save(user));
    }

    @Override
    public void delete(UUID id) {
        User user = repository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        user.setStatus(Status.EXC);
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getStatus()
        );
    }
}
