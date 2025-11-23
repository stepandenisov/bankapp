package ru.yandex.account.service;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.account.model.dto.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    private static final String TOPIC = "notifications";

    private final Tracer tracer;

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void send(String message) {
        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.debug("Send notification: " + message);
        ThreadContext.clearAll();
        NotificationRequest request = new NotificationRequest(message);
        kafkaTemplate.send(TOPIC, request);
    }
}

