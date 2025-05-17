package com.financeiro.api.service.parser;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.repository.CategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BancoInterCsvParser implements BankCsvParser {

    private final CategoryRepository categoryRepository;

    public BancoInterCsvParser(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 6) {
                if (line.contains("Data Lançamento") && line.contains("Histórico") && line.contains("Valor")) {
                    return true;
                }
                count++;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public List<TransactionRequestDTO> parse(MultipartFile file, User user, UUID accountId) {
        List<TransactionRequestDTO> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount <= 4) continue;
                if (line.trim().isEmpty() || line.startsWith("Data Lançamento")) continue;

                String[] fields = line.split(";", -1);
                if (fields.length < 5 || fields[0].isBlank()) continue;

                String data = fields[0].trim();
                String historico = fields[1].trim();
                String descricao = fields[2].trim();
                String valorStr = fields[3].replace(".", "").replace(",", ".").trim();

                if (valorStr.isBlank()) continue;

                BigDecimal valor = new BigDecimal(valorStr);
                TransactionType tipo = valor.compareTo(BigDecimal.ZERO) < 0 ? TransactionType.DESPESA : TransactionType.RECEITA;

                Category category = categoryRepository.findAllByUser(user).stream()
                        .filter(c -> c.getType() == (tipo == TransactionType.RECEITA ? CategoryType.REVENUE : CategoryType.EXPENSE))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Categoria padrão não encontrada."));

                UUID finalAccountId = accountId != null ? accountId : (
                        category.getAccount() != null ? category.getAccount().getId() : null
                );

                if (finalAccountId == null)
                    throw new RuntimeException("Conta bancária não especificada nem associada à categoria.");

                TransactionRequestDTO dto = new TransactionRequestDTO(
                        finalAccountId,
                        category.getId(),
                        null,
                        historico,
                        tipo,
                        Status.SIM,
                        LocalDate.parse(data, formatter).atStartOfDay(),
                        valor.abs().doubleValue(),
                        descricao,
                        TransactionState.EFFECTIVE,
                        "Importado via CSV",
                        Frequency.NON_RECURRING,
                        1,
                        null,
                        false
                );

                transactions.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar CSV Banco Inter: " + e.getMessage(), e);
        }
        return transactions;
    }
}
