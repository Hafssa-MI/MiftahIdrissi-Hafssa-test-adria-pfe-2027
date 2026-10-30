package net.hmi.bank.account.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hmi.bank.account.service.AccountService;
import net.hmi.bank.account.service.dto.AccountCreateDTO;
import net.hmi.bank.account.service.dto.AccountResponseDTO;
import net.hmi.bank.account.service.dto.BalanceOperationDTO;
import net.hmi.bank.account.service.dto.StatusUpdateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDTO create(@RequestBody @Valid AccountCreateDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<AccountResponseDTO> findAll(@RequestParam(required = false) String customerReference) {
        return customerReference == null ? service.findAll() : service.findByCustomer(customerReference);
    }

    @GetMapping("/{id}")
    public AccountResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public AccountResponseDTO updateStatus(@PathVariable UUID id, @RequestBody @Valid StatusUpdateDTO dto) {
        return service.updateStatus(id, dto.status());
    }

    @PostMapping("/{id}/debit")
    public AccountResponseDTO debit(@PathVariable UUID id, @RequestBody @Valid BalanceOperationDTO dto) {
        return service.debit(id, dto.amount());
    }

    @PostMapping("/{id}/credit")
    public AccountResponseDTO credit(@PathVariable UUID id, @RequestBody @Valid BalanceOperationDTO dto) {
        return service.credit(id, dto.amount());
    }
}