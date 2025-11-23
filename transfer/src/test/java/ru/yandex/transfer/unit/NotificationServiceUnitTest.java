package ru.yandex.transfer.unit;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.transfer.model.NotificationRequest;
import ru.yandex.transfer.service.NotificationService;

import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Mock
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(tracer.currentSpan().context().traceId()).thenReturn("");
        when(tracer.currentSpan().context().spanId()).thenReturn("");
    }

    @Test
    void shouldSendNotificationSuccessfully() {
        String message = "Hello, world!";
        NotificationRequest request = new NotificationRequest(message);

        when(kafkaTemplate.send("notifications", request)).thenReturn(null);

        notificationService.send(message);

        verify(kafkaTemplate, times(1)).send("notifications", request);
    }
}
