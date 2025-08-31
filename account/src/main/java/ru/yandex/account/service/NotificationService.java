package ru.yandex.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.account.model.dto.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    public boolean send(String message){
        NotificationRequest notificationRequest = new NotificationRequest(message);
        HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(notificationRequest);
        return restTemplate.exchange("http://notification/",
                HttpMethod.POST,
                requestEntity,
                String.class)
                .getStatusCode().is2xxSuccessful();
    }

}
