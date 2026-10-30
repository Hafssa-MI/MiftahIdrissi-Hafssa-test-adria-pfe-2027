package net.hmi.bank.transaction.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hmi.bank.transaction.service.TransactionService;
import net.hmi.bank.transaction.service.dto.TransactionCreateDTO;
import net.hmi.bank.transaction.service.dto.TransactionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO create(@RequestBody @Valid TransactionCreateDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<TransactionResponseDTO> findAll(@RequestParam(required = false) UUID accountId) {
        return accountId == null ? service.findAll() : service.findByAccount(accountId);
    }

    @GetMapping("/{id}")
    public TransactionResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }
}