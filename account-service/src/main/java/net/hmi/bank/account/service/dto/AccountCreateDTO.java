package net.hmi.bank.account.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record AccountCreateDTO(
        @NotBlank String customerReference,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}", message = "currency must be a 3-letter code") String currency,
        @PositiveOrZero double initialBalance) {
}