package com.financeiro.api.service;

import com.financeiro.api.dto.cardDTO.CardRequestDTO;
import com.financeiro.api.dto.cardDTO.CardResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CardService {

    public CardResponseDTO create(CardRequestDTO dto);
    public List<CardResponseDTO> findAll();
    public CardResponseDTO findById(UUID id);
    public CardResponseDTO update(UUID id, CardRequestDTO dto);
    public void delete(UUID id);
}
