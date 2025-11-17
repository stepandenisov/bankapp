package ru.yandex.exchange_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class ScheduledDispatchService {

    private final DispatchService dispatchService;

    @Scheduled(fixedRate = 1000)
    public void sendExchangeRate() {
        dispatchService.sendExchangeRate();
    }


}
