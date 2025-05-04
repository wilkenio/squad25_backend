package com.financeiro.api.service;

import com.financeiro.api.dto.userDTO.UserRequestDTO;
import com.financeiro.api.dto.userDTO.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO create(UserRequestDTO dto);
    List<UserResponseDTO> findAll();
    UserResponseDTO findById(UUID id);
    UserResponseDTO update(UUID id, UserRequestDTO dto);
    void delete(UUID id);
}
