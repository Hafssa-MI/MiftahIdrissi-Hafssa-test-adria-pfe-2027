package net.hmi.bank.transaction.repository;

import net.hmi.bank.transaction.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<BankTransaction, UUID> {
    List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}