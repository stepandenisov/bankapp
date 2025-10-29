package ru.yandex.transfer.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.service.BlockerService;
import ru.yandex.transfer.service.NotificationService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @InjectMocks
    private NotificationService notificationService;

    private CircuitBreaker circuitBreaker;
    private Retry retry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        circuitBreaker = CircuitBreaker.ofDefaults("notificationApi");
        retry = Retry.ofDefaults("notificationApi");

        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);
    }


    @Test
    void shouldSendNotificationSuccessfully() {
        String message = "Hello, world!";
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(response);

        notificationService.send(message);

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void shouldHandleNon2xxResponseGracefully() {
        String message = "Server error";
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(response);

        notificationService.send(message);

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void shouldUseCircuitBreakerAndRetry() {
        String message = "Check Resilience4j";
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        notificationService.send(message);

        verify(cbRegistry, times(1)).circuitBreaker("notificationApi");
        verify(retryRegistry, times(1)).retry("notificationApi");
    }
}
