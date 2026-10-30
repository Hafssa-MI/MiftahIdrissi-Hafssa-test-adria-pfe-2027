package net.hmi.bank.transaction.client.dto;

import java.math.BigDecimal;

public record BalanceOperationDTO(BigDecimal amount) {
}