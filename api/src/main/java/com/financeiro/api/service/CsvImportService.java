package com.financeiro.api.service;

import com.financeiro.api.domain.User;
import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.service.parser.BankCsvParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CsvImportService {

    private final List<BankCsvParser> parsers;
    private final TransactionService transactionService;

    public CsvImportService(List<BankCsvParser> parsers, TransactionService transactionService) {
        this.parsers = parsers;
        this.transactionService = transactionService;
    }

    public void importFromCsv(MultipartFile file, User user) {
        parsers.stream()
            .filter(p -> p.supports(file))
            .findFirst()
            .ifPresentOrElse(p -> {
                List<TransactionRequestDTO> dtos = p.parse(file, user);
                dtos.forEach(transactionService::create);
            }, () -> {
                throw new RuntimeException("Formato de CSV não suportado.");
            });
    }
}
