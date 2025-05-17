package com.financeiro.api.service.parser;

import com.financeiro.api.domain.User;
import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BankCsvParser {
    boolean supports(MultipartFile file);
    List<TransactionRequestDTO> parse(MultipartFile file, User user, UUID accountId);
}
