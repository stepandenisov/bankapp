package ru.yandex.exchange_generator.unit.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.exchange_generator.model.Currency;
import ru.yandex.exchange_generator.model.ExchangeRateRequest;
import ru.yandex.exchange_generator.service.DispatchService;
import ru.yandex.exchange_generator.service.GenerationService;
import ru.yandex.exchange_generator.unit.configuration.TestSecurityConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@EmbeddedKafka(
        topics = {"exchange"},
        partitions = 1
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@SpringBootTest
public class DispatchServiceIntegrationTest {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private ConsumerFactory<String, ExchangeRateRequest> consumerFactory;

    @MockBean
    private GenerationService generationService;

    @Test
    void sendExchangeRate_shouldSendMessageToKafka(@Autowired EmbeddedKafkaBroker embeddedKafka) {

        Mockito.when(generationService.generateForCurrency(any()))
                .thenReturn(100.0);

        dispatchService.sendExchangeRate();

        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);

        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ExchangeRateRequest.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        Consumer<String, ExchangeRateRequest> consumer =
                new DefaultKafkaConsumerFactory<>(
                        consumerProps,
                        new StringDeserializer(),
                        new JsonDeserializer<>(ExchangeRateRequest.class)
                ).createConsumer();

        consumer.subscribe(List.of("exchange"));

        ConsumerRecord<String, ExchangeRateRequest> record =
                KafkaTestUtils.getSingleRecord(consumer, "exchange", Duration.ofSeconds(5));

        assertNotNull(record);
        assertNotNull(record.value());

        ExchangeRateRequest request = record.value();

        assertEquals(Currency.values().length, request.getExchangeRate().size());
    }

}
