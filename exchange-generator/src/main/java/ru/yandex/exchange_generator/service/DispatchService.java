package ru.yandex.exchange_generator.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.exchange_generator.model.Currency;
import ru.yandex.exchange_generator.model.Exchange;
import ru.yandex.exchange_generator.model.ExchangeRateRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final GenerationService generationService;

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Scheduled(fixedRate = 1000)
    public void sendExchangeRate() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("exchangeApi");
        Retry retry = retryRegistry.retry("exchangeApi");

        Supplier<Boolean> supplier = () -> {
            List<Exchange> rate = Arrays.stream(Currency.values())
                    .map(currency -> new Exchange(currency, generationService.generateForCurrency(currency)))
                    .toList();

            ExchangeRateRequest request = new ExchangeRateRequest(rate);
            HttpEntity<ExchangeRateRequest> requestEntity = new HttpEntity<>(request);

            return restTemplate.exchange("http://exchange/rate",
                            HttpMethod.POST,
                            requestEntity,
                            String.class)
                    .getStatusCode()
                    .is2xxSuccessful();
        };

        Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception e) {
            System.err.println("Fallback sendExchangeRate: " + e.getMessage());
        }
    }


}
