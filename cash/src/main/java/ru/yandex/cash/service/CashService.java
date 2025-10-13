package ru.yandex.cash.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
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

    private boolean changeAccountReminder(String uri, CashRequest cashRequest) {
        if (blockerService.checkSuspicious()) {
            notificationService.send("Подозрительная операция заблокирована.");
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
                notificationService.send("Операция выполнена.");
                return true;
            } else {
                notificationService.send("Операция не выполнена.");
                return false;
            }
        } catch (HttpClientErrorException e) {
            return false;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }


    public boolean withdraw(Long accountId, CashRequest cashRequest) {
        return changeAccountReminder("http://account/accounts/" + accountId + "/withdraw", cashRequest);
    }

    public boolean topUp(Long accountId, CashRequest cashRequest) {
        return changeAccountReminder("http://account/accounts/" + accountId + "/top-up", cashRequest);
    }
}
