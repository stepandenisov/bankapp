package ru.yandex.notifications.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import ru.yandex.notifications.model.NotificationRequest;
import ru.yandex.notifications.service.NotificationService;

import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private Retry retry;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        circuitBreaker = CircuitBreaker.ofDefaults("frontApi");
        retry = Retry.ofDefaults("frontApi");
        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

    }

    @Test
    void send_shouldCallRestTemplate() {
        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test");

        Supplier<Void> supplier = () -> {
            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request);
            restTemplate.exchange("http://front/notification",
                    HttpMethod.POST,
                    entity,
                    Void.class);
            return null;
        };

        notificationService.send(request);

        verify(restTemplate, times(1)).exchange(
                eq("http://front/notification"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(cbRegistry).circuitBreaker("frontNotificationApi");
        verify(retryRegistry).retry("frontNotificationApi");
    }
}
