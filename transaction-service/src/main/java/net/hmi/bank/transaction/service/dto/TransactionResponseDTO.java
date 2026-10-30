package net.hmi.bank.transaction.service.dto;

import net.hmi.bank.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDTO(UUID id, UUID accountId, BigDecimal amount, String currency,
                                     String merchantCategory, String country, Instant createdAt,
                                     TransactionType type, UUID transferId) {
}