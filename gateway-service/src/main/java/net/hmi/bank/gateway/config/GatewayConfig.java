package net.hmi.bank.gateway.config;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service", r -> r.path("/accounts/**")
                        .uri("lb://account-service"))
                .route("transaction-service", r -> r.path("/transactions/**", "/transfers/**")
                        .uri("lb://transaction-service"))
                .build();
    }
}