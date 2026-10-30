package net.hmi.bank.transaction.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreateDTO(
        @NotNull UUID accountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String merchantCategory,
        @NotBlank String country) {
}