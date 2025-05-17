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
public class PagBankCsvParser implements BankCsvParser {

    private final CategoryRepository categoryRepository;

    public PagBankCsvParser(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            return header != null && header.contains("CODIGO DA TRANSACAO") && header.contains("TIPO") && header.contains("VALOR");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<TransactionRequestDTO> parse(MultipartFile file, User user, UUID accountId) {
        List<TransactionRequestDTO> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length < 5) continue;

                String data = fields[1].trim();
                String tipoDesc = fields[2].trim();
                String descricao = fields[3].trim();
                String valorStr = fields[4].trim().replace(".", "").replace(",", ".");

                if (data.isBlank() || valorStr.isBlank()) continue;

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
                        tipoDesc,
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
            throw new RuntimeException("Erro ao processar CSV PagBank: " + e.getMessage(), e);
        }

        return transactions;
    }
}
