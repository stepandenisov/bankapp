package ru.yandex.exchange_generator.configuration;

import brave.kafka.clients.KafkaTracing;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaZipkinConfig {

    @Bean
    public KafkaTracing kafkaTracing(brave.Tracing tracing) {
        return KafkaTracing.create(tracing);
    }

    @Bean
    public ProducerFactory<Object, Object> tracingProducerFactory(
            KafkaTracing kafkaTracing,
            org.springframework.boot.autoconfigure.kafka.KafkaProperties properties
    ) {
        DefaultKafkaProducerFactory<Object, Object> pf =
                new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());

        return () -> kafkaTracing.producer(pf.createProducer());
    }
}