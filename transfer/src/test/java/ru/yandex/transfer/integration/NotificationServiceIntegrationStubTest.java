package ru.yandex.transfer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.transfer.service.NotificationService;

class NotificationServiceIntegrationStubTest extends BaseServiceStubIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void send_ShouldCallNotificationStubSuccessfully() {
        notificationService.send("Тестовое уведомление");
    }
}
