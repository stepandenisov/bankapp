package ru.yandex.front.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.Account;
import ru.yandex.front.model.AddAccountRequest;
import ru.yandex.front.model.Currency;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RestTemplate restTemplate;

    public List<Account> getAccounts() {
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return List.of(restTemplate.exchange("http://account/accounts/",
                HttpMethod.GET,
                entity,
                Account[].class).getBody());
    }

    public boolean addAccount(Currency currency){
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddAccountRequest> entity = new HttpEntity<>(new AddAccountRequest(currency), headers);
        try {
            restTemplate.exchange("http://account/accounts/",
                    HttpMethod.POST,
                    entity,
                    Void.class);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean deleteAccount(Long accountId){
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange("http://account/accounts/" + accountId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class);
            return true;
        } catch (Exception e){
            return false;
        }
    }

}
