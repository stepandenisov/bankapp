package ru.yandex.front.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.Currency;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    public List<Currency> getCurrencies() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("currencyApi");
        Retry retry = retryRegistry.retry("currencyApi");

        Supplier<List<Currency>> supplier = () -> {
            Currency[] response = restTemplate.getForObject("http://exchange/rate/currencies", Currency[].class);
            return response != null ? List.of(response) : List.of();
        };

        return Decorators.ofSupplier(supplier)
                .withCircuitBreaker(cb)
                .withRetry(retry)
                .withFallback(ex -> {
                    return List.of();
                })
                .get();
    }

    public List<Currency> fallbackCurrencyResponse(Throwable ex) {
        return List.of();
    }


}
