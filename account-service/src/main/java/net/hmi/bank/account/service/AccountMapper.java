package net.hmi.bank.account.service;

import net.hmi.bank.account.domain.Account;
import net.hmi.bank.account.service.dto.AccountResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponseDTO toDto(Account a) {
        return new AccountResponseDTO(a.getId(), a.getCustomerReference(), a.getStatus(),
                a.getCurrency(), a.getBalance());
    }
}