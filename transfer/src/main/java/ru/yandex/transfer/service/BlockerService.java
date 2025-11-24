package ru.yandex.transfer.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BlockerService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${blocker.uri}")
    private String blockerUri;

    private static final Logger log = LoggerFactory.getLogger(BlockerService.class);

    private final Tracer tracer;

    public boolean checkSuspicious() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("blockerApi");
        Retry retry = retryRegistry.retry("blockerApi");

        Supplier<Boolean> supplier = () -> restTemplate.getForObject(blockerUri+"/", Boolean.class);

        Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            return protectedCall.get();
        } catch (Exception e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot check suspicious.");
            ThreadContext.clearAll();
            return false;
        }
    }

}
