package net.hmi.bank.transaction.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hmi.bank.transaction.service.TransactionService;
import net.hmi.bank.transaction.service.dto.TransferRequestDTO;
import net.hmi.bank.transaction.service.dto.TransferResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransactionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponseDTO transfer(@RequestBody @Valid TransferRequestDTO dto) {
        return service.transfer(dto);
    }
}