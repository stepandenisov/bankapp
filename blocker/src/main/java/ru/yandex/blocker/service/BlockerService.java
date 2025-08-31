package ru.yandex.blocker.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class BlockerService {

    public boolean isSuspicious(){
        return ThreadLocalRandom.current().nextDouble(0, 1) < 0.1;
    }

}
