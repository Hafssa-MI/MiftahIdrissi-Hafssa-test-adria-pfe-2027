package net.hmi.bank.transaction.client;

import net.hmi.bank.transaction.client.dto.AccountDTO;
import net.hmi.bank.transaction.client.dto.BalanceOperationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "account-service",
        url = "${bank.clients.account-service.url:}",
        configuration = AccountClientConfig.class,
        fallbackFactory = AccountClientFallbackFactory.class)
public interface AccountClient {

    @GetMapping("/accounts/{id}")
    AccountDTO getAccount(@PathVariable("id") UUID id);

    @PostMapping("/accounts/{id}/debit")
    AccountDTO debit(@PathVariable("id") UUID id, @RequestBody BalanceOperationDTO operation);

    @PostMapping("/accounts/{id}/credit")
    AccountDTO credit(@PathVariable("id") UUID id, @RequestBody BalanceOperationDTO operation);
}