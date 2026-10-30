package net.hmi.bank.transaction.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "bank.transaction")
@Getter
@Setter
public class BankTransactionProperties {
    private BigDecimal maxAmount = new BigDecimal("1000");
}