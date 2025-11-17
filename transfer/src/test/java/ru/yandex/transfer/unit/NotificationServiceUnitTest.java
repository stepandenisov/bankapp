package ru.yandex.transfer.unit;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
