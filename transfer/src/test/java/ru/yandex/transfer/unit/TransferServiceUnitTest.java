package ru.yandex.transfer.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.model.*;
import ru.yandex.transfer.service.BlockerService;
import ru.yandex.transfer.service.NotificationService;
import ru.yandex.transfer.service.TransferService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransferServiceUnitTest {

    @InjectMocks
    private TransferService transferService;

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

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private Retry retry;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        circuitBreaker = CircuitBreaker.ofDefaults("blockerApi");
        retry = Retry.ofDefaults("blockerApi");
        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn("fake-token");

        try {
            var field = TransferService.class.getDeclaredField("accountUri");
            field.setAccessible(true);
            field.set(transferService, "http://account");

            field = TransferService.class.getDeclaredField("exchangeUri");
            field.setAccessible(true);
            field.set(transferService, "http://exchange");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSelfTransfer_Success() {
        when(blockerService.checkSuspicious()).thenReturn(false);

        Account from = new Account(1L, Currency.USD, 100.0);
        Account to = new Account(2L, Currency.RUB, 100.0);
        when(restTemplate.exchange(eq("http://account/accounts"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Account[].class)))
                .thenReturn(new ResponseEntity<>(new Account[]{from, to}, HttpStatus.OK));

        ExchangeResponse exchangeResponse = new ExchangeResponse();
        exchangeResponse.setValue(90.0);
        when(restTemplate.exchange(eq("http://exchange/rate/convert?from={from}&to={to}&amount={amount}"),
                eq(HttpMethod.GET), isNull(), eq(ExchangeResponse.class), anyMap()))
                .thenReturn(new ResponseEntity<>(exchangeResponse, HttpStatus.OK));

        when(restTemplate.exchange(contains("/withdraw"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        when(restTemplate.exchange(contains("/top-up"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        SelfTransferRequest request = new SelfTransferRequest(1L, 2L, 100.0);

        boolean result = transferService.selfTransfer(request);

        assertTrue(result);
        verify(notificationService).send("Перевод осуществлен.");
    }

    @Test
    void testSelfTransfer_SuspiciousBlocked() {
        when(blockerService.checkSuspicious()).thenReturn(true);
        SelfTransferRequest request = new SelfTransferRequest(1L, 2L, 100.0);

        boolean result = transferService.selfTransfer(request);

        assertFalse(result);
        verify(notificationService).send("Подозрительная операция заблокирована.");
    }

}
