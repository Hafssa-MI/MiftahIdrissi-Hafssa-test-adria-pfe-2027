package net.hmi.bank.account.web.graphql;

import lombok.RequiredArgsConstructor;
import net.hmi.bank.account.domain.AccountStatus;
import net.hmi.bank.account.service.AccountService;
import net.hmi.bank.account.service.dto.AccountCreateDTO;
import net.hmi.bank.account.service.dto.AccountResponseDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQlController {

    private final AccountService service;

    public record CreateAccountInput(String customerReference, String currency, Double initialBalance) {
    }

    @QueryMapping
    public List<AccountResponseDTO> accounts(@Argument String customerReference) {
        return customerReference == null ? service.findAll() : service.findByCustomer(customerReference);
    }

    @QueryMapping
    public AccountResponseDTO account(@Argument UUID id) {
        return service.findById(id);
    }

    @MutationMapping
    public AccountResponseDTO createAccount(@Argument CreateAccountInput input) {
        double initial = input.initialBalance() == null ? 0 : input.initialBalance();
        return service.create(new AccountCreateDTO(input.customerReference(), input.currency(), initial));
    }

    @MutationMapping
    public AccountResponseDTO updateAccountStatus(@Argument UUID id, @Argument AccountStatus status) {
        return service.updateStatus(id, status);
    }

    @MutationMapping
    public AccountResponseDTO debitAccount(@Argument UUID id, @Argument double amount) {
        return service.debit(id, BigDecimal.valueOf(amount));
    }

    @MutationMapping
    public AccountResponseDTO creditAccount(@Argument UUID id, @Argument double amount) {
        return service.credit(id, BigDecimal.valueOf(amount));
    }
}