package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Card;
import com.financeiro.api.dto.cardDTO.CardRequestDTO;
import com.financeiro.api.dto.cardDTO.CardResponseDTO;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.CardRepository;
import com.financeiro.api.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    public CardResponseDTO create(CardRequestDTO dto) {
        Card card = new Card();
        card.setName(dto.name());
        card.setStatus(dto.status());

        Card saved = cardRepository.save(card);
        return toResponse(saved);
    }

    public List<CardResponseDTO> findAll() {
        return cardRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CardResponseDTO findById(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );
        return toResponse(card);
    }

    public CardResponseDTO update(UUID id, CardRequestDTO dto) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );
        card.setName(dto.name());
        card.setStatus(dto.status());

        Card updated = cardRepository.save(card);
        return toResponse(updated);
    }

    public void delete(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );
        card.setStatus(com.financeiro.api.domain.enums.Status.EXC);
        cardRepository.save(card);
    }

    private CardResponseDTO toResponse(Card card) {
        return new CardResponseDTO(
                card.getId(),
                card.getName(),
                card.getStatus(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
