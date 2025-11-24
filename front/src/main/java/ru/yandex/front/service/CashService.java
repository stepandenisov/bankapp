package ru.yandex.front.service;

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

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final Tracer tracer;

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
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Withdraw success.");
            ThreadContext.clearAll();
        } catch (Exception e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot withdraw for user.");
            ThreadContext.clearAll();
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
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Top up success.");
            ThreadContext.clearAll();
        } catch (Exception e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot top up accounts");
            ThreadContext.clearAll();
        }
    }

}
