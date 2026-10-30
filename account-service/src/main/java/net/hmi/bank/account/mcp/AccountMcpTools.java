package net.hmi.bank.account.mcp;

import lombok.RequiredArgsConstructor;
import net.hmi.bank.account.domain.AccountStatus;
import net.hmi.bank.account.service.AccountService;
import net.hmi.bank.account.service.dto.AccountCreateDTO;
import net.hmi.bank.account.service.dto.AccountResponseDTO;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountMcpTools {

    private final AccountService service;

    @McpTool(name = "list_accounts",
            description = "List bank accounts. Optionally filter by customer reference.")
    public List<AccountResponseDTO> listAccounts(
            @McpToolParam(description = "Customer reference to filter by", required = false) String customerReference) {
        return (customerReference == null || customerReference.isBlank())
                ? service.findAll() : service.findByCustomer(customerReference);
    }

    @McpTool(name = "get_account",
            description = "Get one bank account (status, currency, balance) by its UUID.")
    public AccountResponseDTO getAccount(
            @McpToolParam(description = "Account UUID") UUID accountId) {
        return service.findById(accountId);
    }

    @McpTool(name = "create_account",
            description = "Create a new ACTIVE bank account for a customer.")
    public AccountResponseDTO createAccount(
            @McpToolParam(description = "Customer reference") String customerReference,
            @McpToolParam(description = "3-letter currency code, e.g. EUR") String currency,
            @McpToolParam(description = "Initial balance, 0 or more", required = false) Double initialBalance) {
        return service.create(new AccountCreateDTO(customerReference, currency,
                initialBalance == null ? 0 : initialBalance));
    }

    @McpTool(name = "update_account_status",
            description = "Block or activate a bank account.")
    public AccountResponseDTO updateAccountStatus(
            @McpToolParam(description = "Account UUID") UUID accountId,
            @McpToolParam(description = "ACTIVE or BLOCKED") AccountStatus status) {
        return service.updateStatus(accountId, status);
    }

    @McpTool(name = "debit_account",
            description = "Withdraw a strictly positive amount from an active account. Fails if funds are insufficient.")
    public AccountResponseDTO debitAccount(
            @McpToolParam(description = "Account UUID") UUID accountId,
            @McpToolParam(description = "Amount to withdraw, greater than 0") double amount) {
        return service.debit(accountId, BigDecimal.valueOf(amount));
    }

    @McpTool(name = "credit_account",
            description = "Deposit a strictly positive amount into an active account.")
    public AccountResponseDTO creditAccount(
            @McpToolParam(description = "Account UUID") UUID accountId,
            @McpToolParam(description = "Amount to deposit, greater than 0") double amount) {
        return service.credit(accountId, BigDecimal.valueOf(amount));
    }
}