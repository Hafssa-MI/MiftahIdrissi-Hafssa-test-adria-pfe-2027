package net.hmi.bank.transaction.service.dto;

import java.util.UUID;

public record TransferResponseDTO(UUID transferId, TransactionResponseDTO debit, TransactionResponseDTO credit) {
}