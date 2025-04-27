package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountRangeValueDTO;
import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountSaveResponseDTO;
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
    public ResponseEntity<AccountSaveResponseDTO> create(@RequestBody AccountRequestDTO dto) {
        return ResponseEntity.ok(accountServiceImpl.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAll() {
        return ResponseEntity.ok(accountServiceImpl.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountServiceImpl.findById(id));
    }

    @GetMapping("/accountName/{accountName}")
    public ResponseEntity<List<AccountResponseDTO>> findByAccountName(@PathVariable String accountName) {
        List<AccountResponseDTO> accounts = accountServiceImpl.findByAccountName(accountName);

        return ResponseEntity.ok(accounts);
    }

    /*
     *AIDCIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
     * KEY= minValue VALUE= 10.00
     * KEY= maxValue VALUE= 1000.00
     */
    @GetMapping("/openingBalance")
    public ResponseEntity<List<AccountResponseDTO>> findByOpeningBalanceBetween(@ModelAttribute AccountRangeValueDTO filter){
        List<AccountResponseDTO> accounts = accountServiceImpl.findByOpeningBalanceBetween(filter.minValue(), filter.maxValue());

        return ResponseEntity.ok(accounts);
    }

    /*
    *AIDCIONE OS DOIS VALORES NO PARAMS CASO VÁ TESTAR. Exemplo:
    * KEY= minValue VALUE= 10.00
    * KEY= maxValue VALUE= 1000.00
    */
    @GetMapping("/specialCheck")
    public ResponseEntity<List<AccountResponseDTO>> findBySpecialCheck(@ModelAttribute AccountRangeValueDTO filter) {
        List<AccountResponseDTO> accounts = accountServiceImpl.findBySpecialCheckBetween(filter.minValue(), filter.maxValue());

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountResponseDTO>> findByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(accountServiceImpl.findByStatus(status));
    }

    public ResponseEntity<AccountResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountServiceImpl.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountSaveResponseDTO> update(@PathVariable UUID id, @RequestBody AccountRequestDTO dto) {
        return ResponseEntity.ok(accountServiceImpl.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}
