package net.hmi.bank.account.service.dto;

import net.hmi.bank.account.domain.AccountStatus;

import java.util.UUID;

public record AccountResponseDTO(UUID id, String customerReference, AccountStatus status,
                                 String currency, double balance) {
}