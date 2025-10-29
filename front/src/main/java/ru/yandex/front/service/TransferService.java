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
import ru.yandex.front.model.ExternalTransferRequest;
import ru.yandex.front.model.SelfTransferRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${transfer.uri}")
    private String transferUri;

    public void selfTransfer(SelfTransferRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("transferApi");
        Retry retry = retryRegistry.retry("transferApi");

        Supplier<Void> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SelfTransferRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    transferUri + "/transfer/self",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception ignored) {
        }
    }

    public void externalTransfer(ExternalTransferRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("transferApi");
        Retry retry = retryRegistry.retry("transferApi");

        Supplier<Void> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ExternalTransferRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    transferUri + "/transfer/external",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception ignored) {
        }
    }


}
