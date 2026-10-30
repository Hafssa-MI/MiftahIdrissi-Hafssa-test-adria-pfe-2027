package net.hmi.bank.transaction.client;

import lombok.extern.slf4j.Slf4j;
import net.hmi.bank.transaction.client.dto.AccountDTO;
import net.hmi.bank.transaction.client.dto.BalanceOperationDTO;
import net.hmi.bank.transaction.exception.BusinessRuleException;
import net.hmi.bank.transaction.exception.RemoteServiceUnavailableException;
import net.hmi.bank.transaction.exception.ResourceNotFoundException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {

    @Override
    public AccountClient create(Throwable cause) {
        return new AccountClient() {
            @Override
            public AccountDTO getAccount(UUID id) {
                throw translate(cause);
            }

            @Override
            public AccountDTO debit(UUID id, BalanceOperationDTO operation) {
                throw translate(cause);
            }

            @Override
            public AccountDTO credit(UUID id, BalanceOperationDTO operation) {
                throw translate(cause);
            }
        };
    }

    private RuntimeException translate(Throwable cause) {
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof BusinessRuleException || t instanceof ResourceNotFoundException) {
                return (RuntimeException) t;
            }
        }
        log.warn("account-service call failed: {}", cause.toString());
        return new RemoteServiceUnavailableException(
                "account-service is currently unavailable, please retry later", cause);
    }
}