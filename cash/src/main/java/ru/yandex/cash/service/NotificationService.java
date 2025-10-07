package ru.yandex.cash.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.cash.model.NotificationRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;


    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;


    public void send(String message) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("notificationApi");
        Retry retry = retryRegistry.retry("notificationApi");

        Supplier<Boolean> supplier = () -> {
            NotificationRequest notificationRequest = new NotificationRequest(message);
            HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(notificationRequest);

            return restTemplate.exchange("http://notification/",
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
        }
    }
}
