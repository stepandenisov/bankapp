package ru.yandex.transfer.service;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;

    private final BlockerService blockerService;

    // TODO удалить токены
    private Long getAccountIdByCurrencyAndUserId(String token, Currency currency, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            Map<String, Object> params = Map.of(
                    "currency", currency.name(),
                    "userId", userId
            );
            return restTemplate.exchange("http://account/accounts/find?currency=" + currency + "&userId=" + userId,
                            HttpMethod.GET,
                            entity,
                            Long.class,
                            params)
                    .getBody();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    private boolean transfer(Long fromAccountId,
                             Long toAccountId,
                             Double fromAmount,
                             Double toAmount,
                             String token) {
        CashRequest withdrawRequest = new CashRequest(fromAmount);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
            restTemplate.exchange("http://account/accounts/" + fromAccountId + "/withdraw",
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
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest, headers);
            restTemplate.exchange("http://account/accounts/" + toAccountId + "/top-up",
                            HttpMethod.POST,
                            entity,
                            Boolean.class)
                    .getBody();
//            }
        } catch (Exception e) {
            // TODO убрать headers

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
            restTemplate.exchange("http://account/accounts/" + fromAccountId + "/top-up",
                            HttpMethod.POST,
                            entity,
                            Boolean.class)
                    .getBody();
            throw new InternalServerErrorException();
        }
        return true;
    }

    public boolean selfTransfer(SelfTransferRequest selfTransferRequest) {

        if (blockerService.checkSuspicious()) {
            return false;
        }

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        List<Account> accounts = List.of(restTemplate.exchange("http://account/accounts/",
                        HttpMethod.GET,
                        entity,
                        Account[].class)
                .getBody());

        Currency fromCurrency = accounts.stream()
                .filter(account -> Objects.equals(account.getId(), selfTransferRequest.getFromAccountId()))
                .findFirst()
                .map(Account::getCurrency)
                .orElseThrow(() -> new NotFoundException("У пользователя нет такого счета"));

        Currency toCurrency = accounts.stream()
                .filter(account -> Objects.equals(account.getId(), selfTransferRequest.getToAccountId()))
                .findFirst()
                .map(Account::getCurrency)
                .orElseThrow(() -> new NotFoundException("У пользователя нет такого счета"));

        Map<String, Object> params = Map.of(
                "from", fromCurrency,
                "to", toCurrency,
                "amount", selfTransferRequest.getAmount()
        );

        Double toAmount;
        try {
            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
                    HttpMethod.GET,
                    null,
                    ExchangeResponse.class,
                    params).getBody();
            toAmount = rateInfo.getValue();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return transfer(selfTransferRequest.getFromAccountId(),
                selfTransferRequest.getToAccountId(),
                selfTransferRequest.getAmount(),
                toAmount,
                token);

    }

    public boolean externalTransfer(ExternalTransferRequest request) throws BadRequestException {

//        if (blockerService.checkSuspicious()) {
//            return false;
//        }

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        Currency fromCurrency;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        List<Account> accounts = List.of(restTemplate.exchange("http://account/accounts/",
                        HttpMethod.GET,
                        entity,
                        Account[].class)
                .getBody());

        fromCurrency = accounts.stream()
                .filter(account -> Objects.equals(account.getId(), request.getFromAccountId()))
                .findFirst()
                .map(Account::getCurrency)
                .orElseThrow(() -> new NotFoundException("У пользователя нет такого счета"));


        Map<String, Object> params = Map.of(
                "userId", request.getUserId(),
                "currency", request.getToCurrency()
        );
        Long toAccountId;
        try {
            toAccountId = restTemplate.exchange("http://account/accounts/findAccountId?userId={userId}&currency={currency}",
                            HttpMethod.GET,
                            entity,
                            Long.class,
                            params)
                    .getBody();
        } catch (Exception e) {
            throw new BadRequestException("У пользователя нет счета с такой валютой");
        }

        params = Map.of(
                "from", fromCurrency,
                "to", request.getToCurrency(),
                "amount", request.getAmount()
        );

        Double toAmount;
        try {
            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
                    HttpMethod.GET,
                    null,
                    ExchangeResponse.class,
                    params).getBody();
            toAmount = rateInfo.getValue();
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return transfer(request.getFromAccountId(),
                toAccountId,
                request.getAmount(),
                toAmount,
                token);
    }

}
