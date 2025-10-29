package ru.yandex.front.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.CashRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CashService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${cash.uri}")
    private String cashUri;

    public void withdraw(Long accountId, Double volume) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("cashApi");
        Retry retry = retryRegistry.retry("cashApi");

        Supplier<Boolean> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(new CashRequest(volume), headers);

            restTemplate.exchange(
                    cashUri + "/withdraw/" + accountId,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return true;
        };

        Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception e) {
        }
    }

    public void topUp(Long accountId, Double volume) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("cashApi");
        Retry retry = retryRegistry.retry("cashApi");

        Supplier<Boolean> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(new CashRequest(volume), headers);

            restTemplate.exchange(
                    cashUri + "/top-up/" + accountId,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return true;
        };

        Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception e) {
        }
    }

}
