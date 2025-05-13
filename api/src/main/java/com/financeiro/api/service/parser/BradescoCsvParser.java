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

@Component
public class BradescoCsvParser implements BankCsvParser {

    private final CategoryRepository categoryRepository;

    public BradescoCsvParser(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Crédito (R$)") && line.contains("Débito (R$)")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public List<TransactionRequestDTO> parse(MultipartFile file, User user) {
        List<TransactionRequestDTO> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1))) {
            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                // Ignorar linhas institucionais ou cabeçalhos duplicados
                if (line.toLowerCase().matches(".*(extrato|filtro|total|lancamentos|resultados).*")) continue;
                if (!line.matches("\\d{2}/\\d{2}/\\d{4};.*")) continue;

                String[] fields = line.split(";", -1);
                if (fields.length < 5) continue;

                String data = fields[0].trim();
                String historico = fields[1].trim();
                String documento = fields[2].trim();
                String credito = fields[3].replace(".", "").replace(",", ".").trim();
                String debito = fields.length > 4 ? fields[4].replace(".", "").replace(",", ".").trim() : "";

                BigDecimal valor;
                TransactionType tipo;

                if (!credito.isEmpty() && !credito.equals("0")) {
                    valor = new BigDecimal(credito);
                    tipo = TransactionType.RECEITA;
                } else if (!debito.isEmpty() && !debito.equals("0")) {
                    valor = new BigDecimal(debito);
                    tipo = TransactionType.DESPESA;
                } else {
                    continue;
                }

                Category category = categoryRepository.findAllByUser(user).stream()
                        .filter(c -> c.getType() == (tipo == TransactionType.RECEITA ? CategoryType.REVENUE : CategoryType.EXPENSE))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Categoria padrão não encontrada."));

                if (category.getAccount() == null)
                    throw new RuntimeException("Categoria sem conta associada.");

                TransactionRequestDTO dto = new TransactionRequestDTO(
                        category.getAccount().getId(),
                        category.getId(),
                        null,
                        historico,
                        tipo,
                        Status.SIM,
                        LocalDate.parse(data, formatter).atStartOfDay(),
                        valor.abs().doubleValue(),
                        documento,
                        TransactionState.EFFECTIVE,
                        "Importado via CSV",
                        Frequency.NON_RECURRING,
                        1
                );

                transactions.add(dto);
            }

            if (transactions.isEmpty()) {
                throw new RuntimeException("Nenhuma transação válida encontrada no extrato do Bradesco.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar CSV Bradesco: " + e.getMessage(), e);
        }
        return transactions;
    }
}