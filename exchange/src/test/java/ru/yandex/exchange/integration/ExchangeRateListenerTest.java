package ru.yandex.exchange.integration;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.exchange.TestSecurityConfig;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.service.ExchangeRateService;

import java.util.List;

import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"notifications"})
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ExchangeRateListenerTest {

    @Autowired
    private KafkaTemplate<String, ExchangeRateRequest> kafkaTemplate;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void whenMessageSent_thenServiceCalled() throws InterruptedException {
        Thread.sleep(1000);
        ExchangeRateRequest request = new ExchangeRateRequest();
        request.setExchangeRate(List.of(
                new Exchange(Currency.RUB, 1.0),
                new Exchange(Currency.CNY, 15.0),
                new Exchange(Currency.USD, 80.0)
        ));

        kafkaTemplate.send("exchange", request);
        Thread.sleep(1000);

        verify(exchangeRateService)
                .setRates(request.getExchangeRate());
    }

}
