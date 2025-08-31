package ru.yandex.transfer.service;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.model.*;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;

    private final BlockerService blockerService;

    // TODO удалить токены
    private Currency getCurrencyById(String token, Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange("http://account/accounts/" + id + "/currency",
                            HttpMethod.GET,
                            entity,
                            Currency.class)
                    .getBody();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    public boolean transfer(TransferRequestDto requestDto) {
        if (blockerService.checkSuspicious()){
            return false;
        }
        Map<String, Object> params = Map.of(
                "from", getCurrencyById(requestDto.getToken(), requestDto.getTransferRequest().getFromAccountId()),
                "to", getCurrencyById(requestDto.getToken(), requestDto.getTransferRequest().getToAccountId()),
                "amount", requestDto.getTransferRequest().getAmount()
        );
        Double toAmount;
        try {
            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
                    HttpMethod.GET,
                    null,
                    ExchangeResponse.class,
                    params).getBody();
            toAmount = rateInfo.getAmount();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
        CashRequest withdrawRequest = new CashRequest(requestDto.getTransferRequest().getAmount());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + requestDto.getToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
            restTemplate.exchange("http://account/accounts/" + requestDto.getTransferRequest().getFromAccountId() + "/withdraw",
                            HttpMethod.POST,
                            entity,
                            Boolean.class)
                    .getBody();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
        try {
            CashRequest topUpRequest = new CashRequest(toAmount);
//            if (requestDto.getTransferRequest().getUserId() == null) {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + requestDto.getToken());
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest, headers);
//                restTemplate.exchange("http://account/accounts/" + requestDto.getTransferRequest().getToAccountId() + "/top-up",
//                                HttpMethod.POST,
//                                entity,
//                                Boolean.class)
//                        .getBody();
//            }
//            else {
            // TODO убрать headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + requestDto.getToken());
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest, headers);
            HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest);
            restTemplate.exchange("http://account/accounts/" + requestDto.getTransferRequest().getToAccountId() + "/top-up",
                            HttpMethod.POST,
                            entity,
                            Boolean.class)
                    .getBody();
//            }
        } catch (Exception e) {
            // TODO убрать headers

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + requestDto.getToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
            restTemplate.exchange("http://account/accounts/" + requestDto.getTransferRequest().getFromAccountId() + "/top-up",
                            HttpMethod.POST,
                            entity,
                            Boolean.class)
                    .getBody();
            throw new InternalServerErrorException();
        }
        return true;
    }

}
