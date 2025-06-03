package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.Status;
// Removido TransactionOrder se não for usado aqui
// Removido SummaryDTO se não for usado aqui
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountRangeValueDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionResponseDTO;
import com.financeiro.api.service.AccountService; // Importar a interface
// import com.financeiro.api.service.impl.AccountServiceImpl; // Usar a interface para injeção é uma boa prática

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Para obter data atual
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService; // Injetar a interface AccountService

    public AccountController(AccountService accountService) { // Construtor usando a interface
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountCalculationResponseDTO> create(@RequestBody AccountCalculationRequestDTO dto) {
        // O método create do serviço não precisa de year/month, pois retorna o estado inicial
        // e o mapAccountToCalculationResponseDTO no service pode usar o mês/ano atual por padrão.
        return ResponseEntity.ok(accountService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountTransactionResponseDTO> update(@PathVariable UUID id,
            @RequestBody AccountTransactionRequestDTO dto) {
        return ResponseEntity.ok(accountService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AccountCalculationResponseDTO>> getAll(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        // Define o ano/mês atual se não forem fornecidos
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(accountService.findAll(effectiveYear, effectiveMonth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountCalculationResponseDTO> findById(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
            
        return ResponseEntity.ok(accountService.findById(id, effectiveYear, effectiveMonth));
    }

    @GetMapping("/accountName/{accountName}")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByAccountName(
            @PathVariable String accountName,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
            
        List<AccountCalculationResponseDTO> accounts = accountService.findByAccountName(accountName, effectiveYear, effectiveMonth);
        return ResponseEntity.ok(accounts);
    }

    /*
     * ADICIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
     * KEY= minValue VALUE= 10.00
     * KEY= maxValue VALUE= 1000.00
     * E AGORA: year=2025&month=6 (opcional)
     */
    @GetMapping("/openingBalance")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByOpeningBalanceBetween(
            @ModelAttribute AccountRangeValueDTO filter, // Para minValue e maxValue
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
            
        List<AccountCalculationResponseDTO> accounts = accountService.findByOpeningBalanceBetween(
                filter.minValue(), filter.maxValue(), effectiveYear, effectiveMonth);
        return ResponseEntity.ok(accounts);
    }

    /*
     * ADICIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
     * KEY= minValue VALUE= 10.00
     * KEY= maxValue VALUE= 1000.00
     * E AGORA: year=2025&month=6 (opcional)
     */
    @GetMapping("/specialCheck")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findBySpecialCheckBetween( // Nome do método no controller pode ser diferente do service se desejar
            @ModelAttribute AccountRangeValueDTO filter,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) { // Renomeado filter para clareza
            
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
            
        List<AccountCalculationResponseDTO> accounts = accountService.findBySpecialCheckBetween(
                filter.minValue(), filter.maxValue(), effectiveYear, effectiveMonth);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByStatus(
            @PathVariable Status status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
            
        return ResponseEntity.ok(accountService.findByStatus(status, effectiveYear, effectiveMonth));
    }
}