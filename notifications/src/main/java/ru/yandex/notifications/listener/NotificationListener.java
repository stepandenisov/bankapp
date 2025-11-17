package ru.yandex.notifications.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.yandex.notifications.model.NotificationRequest;
import ru.yandex.notifications.service.NotificationService;

@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "notifications-service")
    public void handle(NotificationRequest request, Acknowledgment ack) {
        try {
            notificationService.send(request);
            ack.acknowledge();
        } catch (Exception ignored){ }
    }
}
