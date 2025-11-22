package ru.yandex.notifications.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.notifications.model.NotificationRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${front.uri}")
    private String frontUri;

    private final MeterRegistry registry;


    public void send(NotificationRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("frontNotificationApi");
        Retry retry = retryRegistry.retry("frontNotificationApi");

        Supplier<Void> supplier = () -> {
            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request);
            restTemplate.exchange("http://front/notification",
                    HttpMethod.POST,
                    entity,
                    Void.class);
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception exception) {
            registry.counter(
                    "notification_fail"
            ).increment();
        }
    }


}
