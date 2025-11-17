package ru.yandex.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.cash.model.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    private static final String TOPIC = "notifications";

    public void send(String message) {
        NotificationRequest request = new NotificationRequest(message);
        kafkaTemplate.send(TOPIC, request);
    }
}

