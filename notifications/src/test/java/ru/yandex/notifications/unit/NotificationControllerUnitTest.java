package ru.yandex.notifications.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.yandex.notifications.controller.NotificationController;
import ru.yandex.notifications.model.NotificationRequest;
import ru.yandex.notifications.service.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationControllerUnitTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void sendNotification_shouldCallServiceAndReturnOk() {
        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test message");

        ResponseEntity<String> response = notificationController.sendNotification(request);

        verify(notificationService).send(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Уведомление отправлено");
    }
}
