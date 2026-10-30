package net.hmi.bank.transaction.service;

import net.hmi.bank.transaction.domain.BankTransaction;
import net.hmi.bank.transaction.service.dto.TransactionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toDto(BankTransaction t) {
        return new TransactionResponseDTO(t.getId(), t.getAccountId(), t.getAmount(), t.getCurrency(),
                t.getMerchantCategory(), t.getCountry(), t.getCreatedAt(), t.getType(), t.getTransferId());
    }
}