package ru.yandex.cash.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BlockerService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    public boolean checkSuspicious() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("blockerApi");
        Retry retry = retryRegistry.retry("blockerApi");

        Supplier<Boolean> supplier = () -> restTemplate.getForObject("http://blocker/", Boolean.class);

        Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            return protectedCall.get();
        } catch (Exception e) {
            System.err.println("Fallback checkSuspicious: " + e.getMessage());
            return false;
        }
    }


}
