package ru.yandex.account.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.account.model.dto.NotificationRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Setter
    private String baseUrl = "http://notification/";

    public void send(String message) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("notificationApi");
        Retry retry = retryRegistry.retry("notificationApi");

        Supplier<Boolean> supplier = () -> {
            NotificationRequest notificationRequest = new NotificationRequest(message);
            HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(notificationRequest);

            return restTemplate.exchange(baseUrl,
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
