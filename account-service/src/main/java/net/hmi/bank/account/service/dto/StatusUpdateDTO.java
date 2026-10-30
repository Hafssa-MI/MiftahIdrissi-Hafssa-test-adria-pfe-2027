package net.hmi.bank.account.service.dto;

import jakarta.validation.constraints.NotNull;
import net.hmi.bank.account.domain.AccountStatus;

public record StatusUpdateDTO(@NotNull AccountStatus status) {
}