package ru.yandex.transfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BlockerService {

    private final RestTemplate restTemplate;

    public boolean checkSuspicious(){
        try {
            return restTemplate.getForObject("http://blocker/", Boolean.class);
        } catch (Exception e){
            return true;
        }
    }

}
