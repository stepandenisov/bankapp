package ru.yandex.notifications.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Tracer tracer;


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
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Send notification.");
            ThreadContext.clearAll();
        } catch (Exception exception) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Error send notifications.");
            ThreadContext.clearAll();
            registry.counter(
                    "notification_fail"
            ).increment();
        }
    }


}
