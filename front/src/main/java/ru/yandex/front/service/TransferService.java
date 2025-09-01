package ru.yandex.front.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.ExternalTransferRequest;
import ru.yandex.front.model.SelfTransferRequest;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;

    public boolean selfTransfer(SelfTransferRequest request){
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SelfTransferRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.exchange("http://transfer/transfer/self",
                    HttpMethod.POST,
                    entity,
                    Void.class);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean externalTransfer(ExternalTransferRequest request){
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExternalTransferRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.exchange("http://transfer/transfer/external",
                    HttpMethod.POST,
                    entity,
                    Void.class);
            return true;
        } catch (Exception e){
            return false;
        }
    }

}
