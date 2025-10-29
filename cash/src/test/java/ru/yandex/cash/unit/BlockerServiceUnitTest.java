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
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlockerServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @InjectMocks
    private BlockerService blockerService;

    private CircuitBreaker circuitBreaker;
    private Retry retry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        circuitBreaker = CircuitBreaker.ofDefaults("blockerApi");
        retry = Retry.ofDefaults("blockerApi");
        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

        blockerService = new BlockerService(restTemplate, cbRegistry, retryRegistry);
        try {
            var field = BlockerService.class.getDeclaredField("blockerUri");
            field.setAccessible(true);
            field.set(blockerService, "http://blocker/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnTrue_whenRestTemplateReturnsTrue() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(String.class)))
                .thenReturn("true");

        boolean result = blockerService.checkSuspicious();

        assertTrue(result);
        verify(restTemplate).getForObject("http://blocker/", String.class);
    }

    @Test
    void shouldReturnFalse_whenRestTemplateReturnsFalse() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(String.class)))
                .thenReturn("false");

        boolean result = blockerService.checkSuspicious();

        assertFalse(result);
        verify(restTemplate).getForObject("http://blocker/", String.class);
    }

    @Test
    void shouldDecorateWithCircuitBreakerAndRetry() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(String.class)))
                .thenReturn("true");

        boolean result = blockerService.checkSuspicious();

        assertTrue(result);
        verify(cbRegistry).circuitBreaker("blockerApi");
        verify(retryRegistry).retry("blockerApi");
    }
}
