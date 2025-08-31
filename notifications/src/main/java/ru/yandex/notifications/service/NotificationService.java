package ru.yandex.notifications.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.notifications.model.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    public boolean send(NotificationRequest request){
        // TODO сделать отправку на фронт
        System.out.println(request.getMessage());
        return true;
    }

}
