package ru.yandex.cash.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.cash.configuration.TestSecurityConfig;
import ru.yandex.cash.model.CashRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CashServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private BlockerService blockerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CircuitBreakerRegistry cbRegistry;
    @Mock
    private RetryRegistry retryRegistry;

    @InjectMocks
    private CashService cashService;

    private CircuitBreaker cb;
    private Retry retry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cb = CircuitBreaker.ofDefaults("test");
        retry = Retry.ofDefaults("test");
        when(cbRegistry.circuitBreaker(anyString())).thenReturn(cb);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

        var auth = new UsernamePasswordAuthenticationToken("user", "pass", Collections.emptyList());
        auth.setDetails("mockToken");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldReturnFalse_whenSuspicious() {
        when(blockerService.checkSuspicious()).thenReturn(true);

        boolean result = cashService.withdraw(1L, new CashRequest());

        assertFalse(result);
        verify(notificationService).send("Подозрительная операция заблокирована.");
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void shouldReturnTrue_whenResponseOk() {
        when(blockerService.checkSuspicious()).thenReturn(false);

        ResponseEntity<Boolean> response = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(response);

        boolean result = cashService.withdraw(1L, new CashRequest());

        assertTrue(result);
        verify(notificationService).send("Операция выполнена.");
    }

    @Test
    void shouldReturnFalse_whenResponseNotOk() {
        when(blockerService.checkSuspicious()).thenReturn(false);

        ResponseEntity<Boolean> response = new ResponseEntity<>(true, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(response);

        boolean result = cashService.topUp(2L, new CashRequest());

        assertFalse(result);
        verify(notificationService).send("Операция не выполнена.");
    }

    @Test
    void shouldReturnFalse_whenHttpClientErrorException() {
        when(blockerService.checkSuspicious()).thenReturn(false);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenThrow(HttpClientErrorException.class);

        boolean result = cashService.topUp(3L, new CashRequest());

        assertFalse(result);
        verify(notificationService, never()).send("Операция выполнена.");
    }

    @Test
    void shouldThrowResponseStatusException_whenUnknownError() {
        when(blockerService.checkSuspicious()).thenReturn(false);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Ошибка сети"));

        assertThrows(ResponseStatusException.class,
                () -> cashService.withdraw(4L, new CashRequest()));

        verify(notificationService, never()).send("Операция выполнена.");
    }

    @Test
    void shouldCallCorrectUris() {
        when(blockerService.checkSuspicious()).thenReturn(false);
        ResponseEntity<Boolean> ok = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Boolean.class)))
                .thenReturn(ok);

        boolean withdrawResult = cashService.withdraw(10L, new CashRequest());
        boolean topUpResult = cashService.topUp(20L, new CashRequest());

        assertTrue(withdrawResult);
        assertTrue(topUpResult);

        verify(restTemplate).exchange(contains("/withdraw"), any(), any(), eq(Boolean.class));
        verify(restTemplate).exchange(contains("/top-up"), any(), any(), eq(Boolean.class));
    }
}
