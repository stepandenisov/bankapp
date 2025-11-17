package ru.yandex.account.integration.service;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.account.configuration.TestSecurityConfig;
import ru.yandex.account.model.dto.NotificationRequest;
import ru.yandex.account.service.NotificationService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedKafka(
        topics = {"notifications"},
        partitions = 1
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@SpringBootTest
public class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void testSend() {
        notificationService.send("Hello!");

        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);

        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationRequest.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        Consumer<String, NotificationRequest> consumer =
                new DefaultKafkaConsumerFactory<>(
                        consumerProps,
                        new StringDeserializer(),
                        new JsonDeserializer<>(NotificationRequest.class)
                ).createConsumer();

        consumer.subscribe(List.of("notifications"));

        var record = KafkaTestUtils.getSingleRecord(consumer, "notifications");

        assertThat(record).isNotNull();
        assertThat(record.value().getMessage()).isEqualTo("Hello!");
    }

}
