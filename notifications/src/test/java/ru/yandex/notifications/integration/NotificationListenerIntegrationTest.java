package ru.yandex.notifications.integration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.notifications.TestSecurityConfig;
import ru.yandex.notifications.listener.NotificationListener;
import ru.yandex.notifications.model.NotificationRequest;
import ru.yandex.notifications.service.NotificationService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "notifications" })
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class NotificationListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private NotificationListener notificationListener;

    @BeforeEach
    void setUp(){
        openMocks(this);
    }

    @Test
    void testListenerIsMocked() {
        assertNotNull(notificationListener);
    }

    @BeforeAll
    static void init(@Autowired EmbeddedKafkaBroker broker) {
        System.setProperty("spring.kafka.bootstrap-servers", broker.getBrokersAsString());
    }

    @Test
    void whenMessageSent_thenNotificationServiceCalled() throws InterruptedException {
        Thread.sleep(1000);
        NotificationRequest request = new NotificationRequest("Hello!");

        kafkaTemplate.send("notifications", request);

        Thread.sleep(1000);

        Mockito.verify(notificationService)
                .send(Mockito.any(NotificationRequest.class));
    }
}
