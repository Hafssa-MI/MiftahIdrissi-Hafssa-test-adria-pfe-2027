package net.hmi.bank.transaction.mcp;

import lombok.RequiredArgsConstructor;
import net.hmi.bank.transaction.service.TransactionService;
import net.hmi.bank.transaction.service.dto.TransactionCreateDTO;
import net.hmi.bank.transaction.service.dto.TransactionResponseDTO;
import net.hmi.bank.transaction.service.dto.TransferRequestDTO;
import net.hmi.bank.transaction.service.dto.TransferResponseDTO;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionMcpTools {

    private final TransactionService service;

    @McpTool(name = "list_transactions",
            description = "List bank transactions (payments and transfer legs), newest first when filtered. "
                    + "Optionally filter by account UUID.")
    public List<TransactionResponseDTO> listTransactions(
            @McpToolParam(description = "Account UUID to filter by", required = false) UUID accountId) {
        return accountId == null ? service.findAll() : service.findByAccount(accountId);
    }

    @McpTool(name = "get_transaction",
            description = "Get one transaction by its UUID.")
    public TransactionResponseDTO getTransaction(
            @McpToolParam(description = "Transaction UUID") UUID transactionId) {
        return service.findById(transactionId);
    }

    @McpTool(name = "create_payment",
            description = "Record a payment that debits an account. Rules: amount strictly positive and at most "
                    + "the configured ceiling (1000), account must exist and be ACTIVE, currency must equal the "
                    + "account currency, balance must cover the amount. The account balance is updated.")
    public TransactionResponseDTO createPayment(
            @McpToolParam(description = "UUID of the account to debit") UUID accountId,
            @McpToolParam(description = "Amount, greater than 0 and at most 1000") double amount,
            @McpToolParam(description = "3-letter currency code, same as the account, e.g. EUR") String currency,
            @McpToolParam(description = "Merchant category, e.g. GROCERY") String merchantCategory,
            @McpToolParam(description = "Country code, e.g. MA") String country) {
        return service.create(new TransactionCreateDTO(accountId, BigDecimal.valueOf(amount),
                currency, merchantCategory, country));
    }

    @McpTool(name = "transfer_funds",
            description = "Transfer money between two different accounts. Rules: both accounts exist and are "
                    + "ACTIVE, both use the given currency, the source balance covers the amount, amount is "
                    + "strictly positive and at most 1000. If the credit fails, the debit is reversed.")
    public TransferResponseDTO transferFunds(
            @McpToolParam(description = "Source account UUID") UUID fromAccountId,
            @McpToolParam(description = "Destination account UUID, different from the source") UUID toAccountId,
            @McpToolParam(description = "Amount, greater than 0 and at most 1000") double amount,
            @McpToolParam(description = "3-letter currency code shared by both accounts, e.g. EUR") String currency) {
        return service.transfer(new TransferRequestDTO(fromAccountId, toAccountId,
                BigDecimal.valueOf(amount), currency));
    }
}