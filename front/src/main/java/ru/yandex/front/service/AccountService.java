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
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.Account;
import ru.yandex.front.model.AddAccountRequest;
import ru.yandex.front.model.Currency;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${account.uri}")
    private String accountUri;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final Tracer tracer;


    public List<Account> getAccounts() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<List<Account>> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            Account[] body = restTemplate.exchange(
                    accountUri +"/accounts",
                    HttpMethod.GET,
                    entity,
                    Account[].class
            ).getBody();

            return body != null ? List.of(body) : List.of();
        };

        Supplier<List<Account>> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            return protectedCall.get();
        } catch (Exception e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot get users accounts");
            ThreadContext.clearAll();
            return List.of();
        }
    }

    public void addAccount(Currency currency) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<Boolean> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AddAccountRequest> entity = new HttpEntity<>(new AddAccountRequest(currency), headers);

            restTemplate.exchange(
                    accountUri + "/accounts",
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
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot add account for user");
            ThreadContext.clearAll();
        }
    }

    public void deleteAccount(Long accountId) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<Boolean> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    accountUri + "/accounts/" + accountId,
                    HttpMethod.DELETE,
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
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Cannot delete user's account: " + accountId.toString());
            ThreadContext.clearAll();
        }
    }
}
