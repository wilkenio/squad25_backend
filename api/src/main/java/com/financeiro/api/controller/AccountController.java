package com.financeiro.api.controller;

import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
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

    @PostMapping("/post")
    public ResponseEntity<AccountResponseDTO> create(@RequestBody AccountRequestDTO dto) {
        return ResponseEntity.ok(accountServiceImpl.create(dto));
    }

    @GetMapping("/get")
    public ResponseEntity<List<AccountResponseDTO>> getAll() {
        return ResponseEntity.ok(accountServiceImpl.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AccountResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountServiceImpl.findById(id));
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<AccountResponseDTO> update(@PathVariable UUID id, @RequestBody AccountRequestDTO dto) {
        return ResponseEntity.ok(accountServiceImpl.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}
