package net.hmi.bank.transaction.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDTO(
        @NotNull UUID fromAccountId,
        @NotNull UUID toAccountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency) {
}