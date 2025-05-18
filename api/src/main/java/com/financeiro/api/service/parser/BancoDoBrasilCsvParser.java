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
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BancoDoBrasilCsvParser implements BankCsvParser {

    private final CategoryRepository categoryRepository;

    public BancoDoBrasilCsvParser(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), Charset.forName("Windows-1252")))) {
            String header = reader.readLine();
            return header != null && header.contains("Data") && header.contains("Lançamento");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<TransactionRequestDTO> parse(MultipartFile file, User user, UUID accountId) {
        if (accountId == null) {
            throw new RuntimeException("O ID da conta bancária é obrigatório para importação.");
        }

        List<TransactionRequestDTO> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), Charset.forName("Windows-1252")))) {

            String line;
            boolean firstLine = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length < 6) continue;

                String date = fields[0].replace("\"", "").trim();
                String title = fields[1].replace("\"", "").trim();
                String details = fields[2].replace("\"", "").trim();
                String value = fields[4].replace("\"", "").replace(".", "").replace(",", ".").trim();

                if (date.equals("00/00/0000") || date.isBlank() || value.isBlank()) continue;
                if (title.toLowerCase().replaceAll("\\s+", "").startsWith("saldo")) continue;

                BigDecimal amount = new BigDecimal(value);
                TransactionType tipo = amount.compareTo(BigDecimal.ZERO) < 0
                        ? TransactionType.DESPESA : TransactionType.RECEITA;

                Category category = categoryRepository.findAllByUser(user).stream()
                        .filter(c -> c.getType() == (tipo == TransactionType.RECEITA ? CategoryType.REVENUE : CategoryType.EXPENSE))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Categoria padrão não encontrada."));

                TransactionRequestDTO dto = new TransactionRequestDTO(
                        accountId,
                        category.getId(),
                        null,
                        title,
                        tipo,
                        Status.SIM,
                        LocalDate.parse(date, formatter).atStartOfDay(),
                        amount.abs().doubleValue(),
                        details,
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
            throw new RuntimeException("Erro ao processar CSV Banco do Brasil: " + e.getMessage(), e);
        }

        return transactions;
    }
}
