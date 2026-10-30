package net.hmi.bank.account.service;

import lombok.RequiredArgsConstructor;
import net.hmi.bank.account.domain.Account;
import net.hmi.bank.account.domain.AccountStatus;
import net.hmi.bank.account.exception.AccountNotFoundException;
import net.hmi.bank.account.exception.BusinessRuleException;
import net.hmi.bank.account.repository.AccountRepository;
import net.hmi.bank.account.service.dto.AccountCreateDTO;
import net.hmi.bank.account.service.dto.AccountResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final AccountMapper mapper;

    @Transactional
    public AccountResponseDTO create(AccountCreateDTO dto) {
        Account account = Account.builder()
                .customerReference(dto.customerReference())
                .currency(dto.currency().toUpperCase())
                .balance(dto.initialBalance())
                .status(AccountStatus.ACTIVE)
                .build();
        return mapper.toDto(repository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findByCustomer(String customerReference) {
        return repository.findByCustomerReference(customerReference).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO findById(UUID id) {
        return mapper.toDto(getEntity(id));
    }

    @Transactional
    public AccountResponseDTO updateStatus(UUID id, AccountStatus status) {
        Account account = getEntity(id);
        account.setStatus(status);
        return mapper.toDto(account);
    }

    @Transactional
    public AccountResponseDTO debit(UUID id, BigDecimal amount) {
        Account account = getEntity(id);
        requireActive(account);
        BigDecimal newBalance = BigDecimal.valueOf(account.getBalance()).subtract(amount);
        if (newBalance.signum() < 0) {
            throw new BusinessRuleException("Insufficient funds on account " + id);
        }
        account.setBalance(newBalance.doubleValue());
        return mapper.toDto(account);
    }

    @Transactional
    public AccountResponseDTO credit(UUID id, BigDecimal amount) {
        Account account = getEntity(id);
        requireActive(account);
        account.setBalance(BigDecimal.valueOf(account.getBalance()).add(amount).doubleValue());
        return mapper.toDto(account);
    }

    private Account getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account " + account.getId() + " is " + account.getStatus());
        }
    }
}