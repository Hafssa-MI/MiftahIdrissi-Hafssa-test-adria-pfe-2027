package net.hmi.bank.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hmi.bank.transaction.client.AccountClient;
import net.hmi.bank.transaction.client.dto.AccountDTO;
import net.hmi.bank.transaction.client.dto.BalanceOperationDTO;
import net.hmi.bank.transaction.config.BankTransactionProperties;
import net.hmi.bank.transaction.domain.BankTransaction;
import net.hmi.bank.transaction.domain.TransactionType;
import net.hmi.bank.transaction.exception.BusinessRuleException;
import net.hmi.bank.transaction.exception.ResourceNotFoundException;
import net.hmi.bank.transaction.repository.TransactionRepository;
import net.hmi.bank.transaction.service.dto.TransactionCreateDTO;
import net.hmi.bank.transaction.service.dto.TransactionResponseDTO;
import net.hmi.bank.transaction.service.dto.TransferRequestDTO;
import net.hmi.bank.transaction.service.dto.TransferResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final BankTransactionProperties properties;
    private final TransactionMapper mapper;

    @Transactional
    public TransactionResponseDTO create(TransactionCreateDTO dto) {
        if (dto.accountId() == null) {
            throw new BusinessRuleException("accountId is required");
        }
        if (dto.merchantCategory() == null || dto.merchantCategory().isBlank()) {
            throw new BusinessRuleException("merchantCategory is required");
        }
        if (dto.country() == null || dto.country().isBlank()) {
            throw new BusinessRuleException("country is required");
        }
        validateAmount(dto.amount());

        // 1. consult the account (existence, status, currency, balance)
        AccountDTO account = accountClient.getAccount(dto.accountId());
        requireUsable(account, dto.currency());
        requireFunds(account, dto.amount());

        // 2. persist the transaction
        BankTransaction saved = repository.save(BankTransaction.builder()
                .accountId(dto.accountId())
                .amount(dto.amount())
                .currency(dto.currency().toUpperCase())
                .merchantCategory(dto.merchantCategory())
                .country(dto.country())
                .createdAt(Instant.now())
                .type(TransactionType.PAYMENT)
                .build());

        // 3. update the balance; if this fails, the saved row is rolled back
        accountClient.debit(dto.accountId(), new BalanceOperationDTO(dto.amount()));

        return mapper.toDto(saved);
    }

    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO dto) {
        if (dto.fromAccountId() == null || dto.toAccountId() == null) {
            throw new BusinessRuleException("fromAccountId and toAccountId are required");
        }
        if (dto.fromAccountId().equals(dto.toAccountId())) {
            throw new BusinessRuleException("Source and destination accounts must be different");
        }
        validateAmount(dto.amount());

        AccountDTO source = accountClient.getAccount(dto.fromAccountId());
        AccountDTO target = accountClient.getAccount(dto.toAccountId());
        requireUsable(source, dto.currency());
        requireUsable(target, dto.currency());
        requireFunds(source, dto.amount());

        UUID transferId = UUID.randomUUID();
        Instant now = Instant.now();
        BankTransaction out = repository.save(
                leg(dto.fromAccountId(), dto, TransactionType.TRANSFER_OUT, transferId, now));
        BankTransaction in = repository.save(
                leg(dto.toAccountId(), dto, TransactionType.TRANSFER_IN, transferId, now));

        BalanceOperationDTO operation = new BalanceOperationDTO(dto.amount());
        accountClient.debit(dto.fromAccountId(), operation);
        try {
            accountClient.credit(dto.toAccountId(), operation);
        } catch (RuntimeException ex) {
            reverseDebit(dto.fromAccountId(), operation, transferId);
            throw ex; // local rows are rolled back too
        }
        return new TransferResponseDTO(transferId, mapper.toDto(out), mapper.toDto(in));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> findByAccount(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO findById(UUID id) {
        return repository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }

    // ---- rules ----

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Amount must be strictly positive");
        }
        if (amount.compareTo(properties.getMaxAmount()) > 0) {
            throw new BusinessRuleException("Amount exceeds the maximum allowed ("
                    + properties.getMaxAmount() + ")");
        }
    }

    private void requireUsable(AccountDTO account, String currency) {
        if (!"ACTIVE".equals(account.status())) {
            throw new BusinessRuleException("Account " + account.id() + " is " + account.status());
        }
        if (currency == null || !account.currency().equalsIgnoreCase(currency)) {
            throw new BusinessRuleException("Currency mismatch: account " + account.id()
                    + " uses " + account.currency() + " but the operation uses " + currency);
        }
    }

    private void requireFunds(AccountDTO account, BigDecimal amount) {
        if (BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds on account " + account.id());
        }
    }

    private BankTransaction leg(UUID accountId, TransferRequestDTO dto, TransactionType type,
                                UUID transferId, Instant now) {
        return BankTransaction.builder()
                .accountId(accountId)
                .amount(dto.amount())
                .currency(dto.currency().toUpperCase())
                .merchantCategory("TRANSFER")
                .createdAt(now)
                .type(type)
                .transferId(transferId)
                .build();
    }

    private void reverseDebit(UUID accountId, BalanceOperationDTO operation, UUID transferId) {
        try {
            accountClient.credit(accountId, operation);
            log.warn("Transfer {} failed: the debit on account {} was reversed", transferId, accountId);
        } catch (RuntimeException ex) {
            log.error("Transfer {} failed AND the reversal on account {} failed: manual reconciliation required",
                    transferId, accountId, ex);
        }
    }
}