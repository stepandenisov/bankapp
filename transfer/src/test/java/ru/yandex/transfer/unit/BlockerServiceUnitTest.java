package ru.yandex.transfer.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.service.BlockerService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        circuitBreaker = CircuitBreaker.ofDefaults("blockerApi");
        retry = Retry.ofDefaults("blockerApi");
        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

        when(tracer.currentSpan().context().traceId()).thenReturn("");
        when(tracer.currentSpan().context().spanId()).thenReturn("");

        blockerService = new BlockerService(restTemplate, cbRegistry, retryRegistry, tracer);
        try {
            var field = BlockerService.class.getDeclaredField("blockerUri");
            field.setAccessible(true);
            field.set(blockerService, "http://blocker");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnTrue_whenRestTemplateReturnsTrue() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(Boolean.class)))
                .thenReturn(Boolean.TRUE);

        boolean result = blockerService.checkSuspicious();

        assertTrue(result);
        verify(restTemplate).getForObject("http://blocker/", Boolean.class);
    }

    @Test
    void shouldReturnFalse_whenRestTemplateReturnsFalse() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(Boolean.class)))
                .thenReturn(Boolean.FALSE);

        boolean result = blockerService.checkSuspicious();

        assertFalse(result);
        verify(restTemplate).getForObject("http://blocker/", Boolean.class);
    }

    @Test
    void shouldDecorateWithCircuitBreakerAndRetry() {
        when(restTemplate.getForObject(eq("http://blocker/"), eq(Boolean.class)))
                .thenReturn(Boolean.TRUE);

        boolean result = blockerService.checkSuspicious();

        assertTrue(result);
        verify(cbRegistry).circuitBreaker("blockerApi");
        verify(retryRegistry).retry("blockerApi");
    }

}
