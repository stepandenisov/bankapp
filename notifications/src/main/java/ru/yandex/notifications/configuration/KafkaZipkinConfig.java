package ru.yandex.notifications.configuration;

import brave.kafka.clients.KafkaTracing;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaZipkinConfig {

    @Bean
    public KafkaTracing kafkaTracing(brave.Tracing tracing) {
        return KafkaTracing.create(tracing);
    }

    @Bean
    public ConsumerFactory<Object, Object> tracingConsumerFactory(
            KafkaProperties kafkaProperties,
            KafkaTracing kafkaTracing
    ) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        return new DefaultKafkaConsumerFactory<>(props) {
            @Override
            public Consumer<Object, Object> createConsumer(String groupId, String clientIdPrefix, String clientIdSuffix, Properties properties) {
                return kafkaTracing.consumer(super.createConsumer(groupId, clientIdPrefix, clientIdSuffix, properties));
            }

            @Override
            public Consumer<Object, Object> createConsumer(String groupId, String clientIdPrefix) {
                return kafkaTracing.consumer(super.createConsumer(groupId, clientIdPrefix));
            }

            @Override
            public Consumer<Object, Object> createConsumer(String groupId) {
                return kafkaTracing.consumer(super.createConsumer(groupId));
            }

            @Override
            public Consumer<Object, Object> createConsumer() {
                return kafkaTracing.consumer(super.createConsumer());
            }
        };
    }
}