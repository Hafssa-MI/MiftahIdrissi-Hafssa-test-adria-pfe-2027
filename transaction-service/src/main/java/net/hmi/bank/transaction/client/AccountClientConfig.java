package net.hmi.bank.transaction.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class AccountClientConfig {

    @Bean
    public ErrorDecoder accountErrorDecoder() {
        return new AccountClientErrorDecoder();
    }
}