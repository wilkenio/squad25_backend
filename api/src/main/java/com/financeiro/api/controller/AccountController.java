package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.*;
import com.financeiro.api.service.impl.AccountServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountServiceImpl accountServiceImpl;

    public AccountController(AccountServiceImpl accountServiceImpl) {
        this.accountServiceImpl = accountServiceImpl;
    }

    @PostMapping
    public ResponseEntity<AccountCalculationResponseDTO> create(@RequestBody AccountCalculationRequestDTO dto) {
        return ResponseEntity.ok(accountServiceImpl.create(dto));
    }

     @PutMapping("/{id}")
     public ResponseEntity<AccountTransactionResponseDTO> update(@PathVariable UUID id, @RequestBody AccountTransactionRequestDTO dto) {
         return ResponseEntity.ok(accountServiceImpl.update(id, dto));
     }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AccountCalculationResponseDTO>> getAll() {
        return ResponseEntity.ok(accountServiceImpl.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountCalculationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountServiceImpl.findById(id));
    }

    @GetMapping("/accountName/{accountName}")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByAccountName(@PathVariable String accountName) {
        List<AccountCalculationResponseDTO> accounts = accountServiceImpl.findByAccountName(accountName);

        return ResponseEntity.ok(accounts);
    }

    /*
     *AIDCIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
     * KEY= minValue VALUE= 10.00
     * KEY= maxValue VALUE= 1000.00
     */
    @GetMapping("/openingBalance")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByOpeningBalanceBetween(@ModelAttribute AccountRangeValueDTO filter){
        List<AccountCalculationResponseDTO> accounts = accountServiceImpl.findByOpeningBalanceBetween(filter.minValue(), filter.maxValue());

        return ResponseEntity.ok(accounts);
    }

    /*
    *AIDCIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
    * KEY= minValue VALUE= 10.00
    * KEY= maxValue VALUE= 1000.00
    */
    @GetMapping("/specialCheck")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findBySpecialCheck(@ModelAttribute AccountRangeValueDTO filter) {
        List<AccountCalculationResponseDTO> accounts = accountServiceImpl.findBySpecialCheckBetween(filter.minValue(), filter.maxValue());

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountCalculationResponseDTO>> findByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(accountServiceImpl.findByStatus(status));
    }


}
