package ru.yandex.cash.service;

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
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.cash.model.CashRequest;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CashService {

    private final RestTemplate restTemplate;

    private final BlockerService blockerService;

    private final NotificationService notificationService;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${account.uri}")
    private String accountUri = "http://accounts/";

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final Tracer tracer;


    private boolean changeAccountReminder(String uri, CashRequest cashRequest) {
        if (blockerService.checkSuspicious()) {
            notificationService.send("Подозрительная операция заблокирована.");
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.info("Suspicious operation");
            ThreadContext.clearAll();
            return false;
        }

        try {
            CircuitBreaker accountCB = cbRegistry.circuitBreaker("accountApi");
            Retry accountRetry = retryRegistry.retry("accountApi");

            Supplier<Boolean> callSupplier = () -> {
                String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Jwt " + token);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CashRequest> requestEntity = new HttpEntity<>(cashRequest, headers);

                return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, Boolean.class)
                        .getStatusCode().equals(HttpStatus.OK);
            };

            Supplier<Boolean> protectedCall = CircuitBreaker.decorateSupplier(accountCB,
                    Retry.decorateSupplier(accountRetry, callSupplier));
            Boolean result = protectedCall.get();
            if (result){
                ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                log.info("Operation complete.");
                ThreadContext.clearAll();
                notificationService.send("Операция выполнена.");
                return true;
            } else {
                ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                log.info("Operation failed.");
                ThreadContext.clearAll();
                notificationService.send("Операция не выполнена.");
                return false;
            }
        } catch (HttpClientErrorException e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Microservice account not available");
            ThreadContext.clearAll();
            return false;
        } catch (Exception e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.error("Cash operation failed because of internal error");
            ThreadContext.clearAll();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }


    public boolean withdraw(Long accountId, CashRequest cashRequest) {
        return changeAccountReminder(accountUri + "accounts/" + accountId + "/withdraw", cashRequest);
    }

    public boolean topUp(Long accountId, CashRequest cashRequest) {
        return changeAccountReminder(accountUri + "accounts/" + accountId + "/top-up", cashRequest);
    }
}
