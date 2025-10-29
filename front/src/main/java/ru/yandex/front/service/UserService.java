package ru.yandex.front.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.*;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${account.uri}")
    private String accountUri;


    public void editPassword(EditPasswordRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<Void> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EditPasswordRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(accountUri + "/user/password", HttpMethod.POST, entity, Void.class);
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception ignored) {
        }
    }

    public void editInfo(EditUserInfoRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<Void> supplier = () -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EditUserInfoRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(accountUri + "/user/info", HttpMethod.POST, entity, Void.class);
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception ignored) {
        }
    }

    public void register(RegisterRequest request) {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<Void> supplier = () -> {
            HttpEntity<RegisterRequest> entity = new HttpEntity<>(request);
            restTemplate.exchange(accountUri + "/auth/register", HttpMethod.POST, entity, Void.class);
            return null;
        };

        Supplier<Void> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            protectedCall.get();
        } catch (Exception ignored) {
        }
    }

    public List<UserDto> getUsers() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("accountApi");
        Retry retry = retryRegistry.retry("accountApi");

        Supplier<List<UserDto>> supplier = () -> {
//            String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            UserDto[] response = restTemplate.exchange(accountUri + "/user/all", HttpMethod.GET, entity, UserDto[].class).getBody();
            return response != null ? List.of(response) : List.of();
        };

        Supplier<List<UserDto>> protectedCall = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, supplier));

        try {
            return protectedCall.get();
        } catch (Exception e) {
            return List.of();
        }
    }


}
