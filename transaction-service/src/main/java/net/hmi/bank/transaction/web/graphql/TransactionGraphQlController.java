package net.hmi.bank.transaction.web.graphql;

import lombok.RequiredArgsConstructor;
import net.hmi.bank.transaction.service.TransactionService;
import net.hmi.bank.transaction.service.dto.TransactionCreateDTO;
import net.hmi.bank.transaction.service.dto.TransactionResponseDTO;
import net.hmi.bank.transaction.service.dto.TransferRequestDTO;
import net.hmi.bank.transaction.service.dto.TransferResponseDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQlController {

    private final TransactionService service;

    public record TransactionInput(UUID accountId, Double amount, String currency,
                                   String merchantCategory, String country) {
    }

    public record TransferInput(UUID fromAccountId, UUID toAccountId, Double amount, String currency) {
    }

    @QueryMapping
    public List<TransactionResponseDTO> transactions(@Argument UUID accountId) {
        return accountId == null ? service.findAll() : service.findByAccount(accountId);
    }

    @QueryMapping
    public TransactionResponseDTO transaction(@Argument UUID id) {
        return service.findById(id);
    }

    @MutationMapping
    public TransactionResponseDTO createTransaction(@Argument TransactionInput input) {
        return service.create(new TransactionCreateDTO(input.accountId(),
                BigDecimal.valueOf(input.amount()), input.currency(),
                input.merchantCategory(), input.country()));
    }

    @MutationMapping
    public TransferResponseDTO transfer(@Argument TransferInput input) {
        return service.transfer(new TransferRequestDTO(input.fromAccountId(), input.toAccountId(),
                BigDecimal.valueOf(input.amount()), input.currency()));
    }
}