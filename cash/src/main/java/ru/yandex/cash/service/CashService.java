package ru.yandex.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.cash.model.CashRequest;
import ru.yandex.cash.model.RequestSpecificInfo;

@Service
@RequiredArgsConstructor
public class CashService {

    private final RestTemplate restTemplate;

    private final BlockerService blockerService;

    private boolean changeAccountReminder(String uri, RequestSpecificInfo requestSpecificInfo){
        if (blockerService.checkSuspicious()){
            return false;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + requestSpecificInfo.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CashRequest> requestEntity = new HttpEntity<>(requestSpecificInfo.getCashRequest(), headers);
        try {
            return restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    Boolean.class
            ).getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return false;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    public boolean withdraw(RequestSpecificInfo requestSpecificInfo){

        return changeAccountReminder("http://account/accounts/" +requestSpecificInfo.getAccountId() +"/withdraw", requestSpecificInfo);
    }

    public boolean topUp(RequestSpecificInfo requestSpecificInfo){
        return changeAccountReminder("http://account/accounts/" +requestSpecificInfo.getAccountId() + "/top-up", requestSpecificInfo);
    }
}
