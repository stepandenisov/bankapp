package ru.yandex.front.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.model.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;

    public boolean editPassword(EditPasswordRequest request) {

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EditPasswordRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange("http://account/user/password",
                HttpMethod.POST,
                entity,
                Void.class).getStatusCode() == HttpStatus.OK;
    }

    public boolean editInfo(EditUserInfoRequest request) {

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EditUserInfoRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange("http://account/user/info",
                HttpMethod.POST,
                entity,
                Void.class).getStatusCode() == HttpStatus.OK;
    }

    public List<UserDto> getUsers() {

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return List.of(restTemplate.exchange("http://account/user/all",
                HttpMethod.GET,
                entity,
                UserDto[].class).getBody());
    }

}
