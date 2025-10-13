package ru.yandex.transfer.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;

    private final BlockerService blockerService;

    private final NotificationService notificationService;


    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    private boolean transfer(Long fromAccountId,
                             Long toAccountId,
                             Double fromAmount,
                             Double toAmount,
                             String token) {

        Supplier<Boolean> withdrawSupplier = () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(
                    "http://account/accounts/" + fromAccountId + "/withdraw",
                    HttpMethod.POST,
                    new HttpEntity<>(new CashRequest(fromAmount), headers),
                    Boolean.class
            );
            return true;
        };

        Supplier<Boolean> topUpSupplier = () -> {
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(
                    "http://account/accounts/" + toAccountId + "/top-up",
                    HttpMethod.POST,
                    new HttpEntity<>(new CashRequest(toAmount), headers),
                    Boolean.class
            );
            return true;
        };

        Supplier<Boolean> withdrawDecorated = CircuitBreaker.decorateSupplier(
                cbRegistry.circuitBreaker("cashApi"),
                Retry.decorateSupplier(retryRegistry.retry("cashApi"), withdrawSupplier)
        );

        Supplier<Boolean> topUpDecorated = CircuitBreaker.decorateSupplier(
                cbRegistry.circuitBreaker("cashApi"),
                Retry.decorateSupplier(retryRegistry.retry("cashApi"), topUpSupplier)
        );

        try {
            withdrawDecorated.get();
        } catch (Exception e) {
            notificationService.send("Ошибка перевода.");
            throw new InternalServerErrorException();
        }
        try {
            topUpDecorated.get();
        } catch (Exception e) {
            Supplier<Boolean> revertTopUpSupplier = () -> {
                HttpHeaders headers = new HttpHeaders();
//                headers.set("Authorization", "Bearer " + token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                restTemplate.exchange(
                        "http://account/accounts/" + fromAccountId + "/top-up",
                        HttpMethod.POST,
                        new HttpEntity<>(new CashRequest(fromAmount), headers),
                        Boolean.class
                );
                return true;
            };

            Supplier<Boolean> revertTopUpDecorated = CircuitBreaker.decorateSupplier(
                    cbRegistry.circuitBreaker("cashApi"),
                    Retry.decorateSupplier(retryRegistry.retry("cashApi"), revertTopUpSupplier)
            );

            notificationService.send("Ошибка перевода.");
            try {
                revertTopUpDecorated.get();
                throw new InternalServerErrorException();
            } catch (Exception e2) {
                throw new InternalServerErrorException();
            }
        }
        notificationService.send("Перевод осуществлен.");
        return true;
    }


    private Double getConvertedAmount(Currency fromCurrency, Currency toCurrency, Double amount) {
        Supplier<Double> supplier = () -> {
            Map<String, Object> params = Map.of(
                    "from", fromCurrency,
                    "to", toCurrency,
                    "amount", amount
            );
            ExchangeResponse rateInfo = restTemplate.exchange(
                    "http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
                    HttpMethod.GET,
                    null,
                    ExchangeResponse.class,
                    params).getBody();
            return rateInfo.getValue();
        };

        Supplier<Double> decorated = CircuitBreaker.decorateSupplier(
                cbRegistry.circuitBreaker("exchangeApi"),
                Retry.decorateSupplier(retryRegistry.retry("exchangeApi"), supplier)
        );

        try {
            return decorated.get();
        } catch (Exception e) {
            notificationService.send("Ошибка перевода.");
            throw new InternalServerErrorException();
        }
    }


    private List<Account> getSelfAccounts() {
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        Supplier<List<Account>> accountsSupplier = () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Jwt " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            return List.of(restTemplate.exchange(
                    "http://account/accounts/",
                    HttpMethod.GET,
                    entity,
                    Account[].class
            ).getBody());
        };

        return Retry.decorateSupplier(retryRegistry.retry("accountApi"),
                CircuitBreaker.decorateSupplier(cbRegistry.circuitBreaker("accountApi"), accountsSupplier)
        ).get();
    }

    private Currency getCurrencyOfAccountById(List<Account> accounts, Long accountId) {
        return accounts.stream()
                .filter(a -> Objects.equals(a.getId(), accountId))
                .map(Account::getCurrency)
                .findFirst()
                .orElseThrow(() -> {
                    notificationService.send("У Вас нет такого счета");
                    return new NotFoundException("У пользователя нет такого счета");
                });
    }

    public boolean selfTransfer(SelfTransferRequest request) {
        if (blockerService.checkSuspicious()) {
            notificationService.send("Подозрительная операция заблокирована.");
            return false;
        }

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();


        List<Account> accounts = getSelfAccounts();

        Currency fromCurrency = getCurrencyOfAccountById(accounts, request.getFromAccountId());

        Currency toCurrency = accounts.stream()
                .filter(a -> Objects.equals(a.getId(), request.getToAccountId()))
                .map(Account::getCurrency)
                .findFirst()
                .orElseThrow(() -> {
                    notificationService.send("У Вас нет такого счета");
                    return new NotFoundException("У пользователя нет такого счета");
                });

        Double toAmount = getConvertedAmount(fromCurrency, toCurrency, request.getAmount());

        return transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount(), toAmount, token);
    }

    public boolean externalTransfer(ExternalTransferRequest request) throws BadRequestException {
        if (blockerService.checkSuspicious()) {
            notificationService.send("Подозрительная операция заблокирована.");
            return false;
        }

        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();

        List<Account> accounts = getSelfAccounts();

        Currency fromCurrency = getCurrencyOfAccountById(accounts, request.getFromAccountId());

        Map<String, Object> params = Map.of(
                "userId", request.getUserId(),
                "currency", request.getToCurrency()
        );

        Long toAccountId;
        try {
            toAccountId = restTemplate.exchange(
                            "http://account/accounts/findAccountId?userId={userId}&currency={currency}",
                            HttpMethod.GET,
                            new HttpEntity<>(new HttpHeaders() {{
                                set("Authorization", "Jwt " + token);
                                setContentType(MediaType.APPLICATION_JSON);
                            }}),
                            Long.class,
                            params)
                    .getBody();
        } catch (Exception e) {
            notificationService.send("У получателя нет счета с такой валютой");
            throw new BadRequestException("У пользователя нет счета с такой валютой");
        }

        Double toAmount = getConvertedAmount(fromCurrency, request.getToCurrency(), request.getAmount());

        return transfer(request.getFromAccountId(), toAccountId, request.getAmount(), toAmount, token);
    }


//    // TODO удалить токены
//
//    private boolean transfer(Long fromAccountId,
//                             Long toAccountId,
//                             Double fromAmount,
//                             Double toAmount,
//                             String token) {
//        CashRequest withdrawRequest = new CashRequest(fromAmount);
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + token);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
//            restTemplate.exchange("http://account/accounts/" + fromAccountId + "/withdraw",
//                            HttpMethod.POST,
//                            entity,
//                            Boolean.class)
//                    .getBody();
//        } catch (Exception e) {
//            notificationService.send("Ошибка перевода.");
//            throw new InternalServerErrorException();
//        }
//        try {
//            CashRequest topUpRequest = new CashRequest(toAmount);
////            if (requestDto.getTransferRequest().getUserId() == null) {
////            HttpHeaders headers = new HttpHeaders();
////            headers.set("Authorization", "Bearer " + requestDto.getToken());
////            headers.setContentType(MediaType.APPLICATION_JSON);
////
////                HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest, headers);
////                restTemplate.exchange("http://account/accounts/" + requestDto.getTransferRequest().getToAccountId() + "/top-up",
////                                HttpMethod.POST,
////                                entity,
////                                Boolean.class)
////                        .getBody();
////            }
////            else {
//            // TODO убрать headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + token);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<CashRequest> entity = new HttpEntity<>(topUpRequest, headers);
//            restTemplate.exchange("http://account/accounts/" + toAccountId + "/top-up",
//                            HttpMethod.POST,
//                            entity,
//                            Boolean.class)
//                    .getBody();
////            }
//        } catch (Exception e) {
//            // TODO убрать headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + token);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<CashRequest> entity = new HttpEntity<>(withdrawRequest, headers);
//            restTemplate.exchange("http://account/accounts/" + fromAccountId + "/top-up",
//                            HttpMethod.POST,
//                            entity,
//                            Boolean.class)
//                    .getBody();
//            throw new InternalServerErrorException();
//        }
//        return true;
//    }
//
//    private Double getConvertedAmount(Currency fromCurrency, Currency toCurrency, Double amount){
//
//        Map<String, Object> params = Map.of(
//                "from", fromCurrency,
//                "to", toCurrency,
//                "amount", amount
//        );
//
//        Double toAmount;
//        try {
//            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
//                    HttpMethod.GET,
//                    null,
//                    ExchangeResponse.class,
//                    params).getBody();
//            toAmount = rateInfo.getValue();
//        } catch (Exception e) {
//            notificationService.send("Ошибка перевода.");
//            throw new InternalServerErrorException();
//        }
//        return toAmount;
//    }
//
//    public boolean selfTransfer(SelfTransferRequest selfTransferRequest) {
//
//        if (blockerService.checkSuspicious()) {
//            notificationService.send("Подозрительная операция заблокирована.");
//            return false;
//        }
//
//        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Object> entity = new HttpEntity<>(headers);
//        List<Account> accounts = List.of(restTemplate.exchange("http://account/accounts/",
//                        HttpMethod.GET,
//                        entity,
//                        Account[].class)
//                .getBody());
//
//        Currency fromCurrency = accounts.stream()
//                .filter(account -> Objects.equals(account.getId(), selfTransferRequest.getFromAccountId()))
//                .findFirst()
//                .map(Account::getCurrency)
//                .orElseThrow(() -> {
//                    notificationService.send("У Вас нет такого счета");
//                    return new NotFoundException("У пользователя нет такого счета");
//                });
//
//        Currency toCurrency = accounts.stream()
//                .filter(account -> Objects.equals(account.getId(), selfTransferRequest.getToAccountId()))
//                .findFirst()
//                .map(Account::getCurrency)
//                .orElseThrow(() -> {
//                    notificationService.send("У Вас нет такого счета");
//                    return new NotFoundException("У пользователя нет такого счета");
//                });
//
//        Map<String, Object> params = Map.of(
//                "from", fromCurrency,
//                "to", toCurrency,
//                "amount", selfTransferRequest.getAmount()
//        );
//
//        Double toAmount = getConvertedAmount(fromCurrency, toCurrency, selfTransferRequest.getAmount());
////        try {
////            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
////                    HttpMethod.GET,
////                    null,
////                    ExchangeResponse.class,
////                    params).getBody();
////            toAmount = rateInfo.getValue();
////        } catch (Exception e) {
////            notificationService.send("Ошибка перевода.");
////            throw new InternalServerErrorException();
////        }
//
//        return transfer(selfTransferRequest.getFromAccountId(),
//                selfTransferRequest.getToAccountId(),
//                selfTransferRequest.getAmount(),
//                toAmount,
//                token);
//
//    }
//
//
//    public boolean externalTransfer(ExternalTransferRequest request) throws BadRequestException {
//
//        if (blockerService.checkSuspicious()) {
//            notificationService.send("Подозрительная операция заблокирована.");
//            return false;
//        }
//
//        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
//
//        Currency fromCurrency;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Object> entity = new HttpEntity<>(headers);
//
//        List<Account> accounts = List.of(restTemplate.exchange("http://account/accounts/",
//                        HttpMethod.GET,
//                        entity,
//                        Account[].class)
//                .getBody());
//
//        fromCurrency = accounts.stream()
//                .filter(account -> Objects.equals(account.getId(), request.getFromAccountId()))
//                .findFirst()
//                .map(Account::getCurrency)
//                .orElseThrow(() -> {
//                    notificationService.send("У Вас нет такого счета");
//                    return new NotFoundException("У пользователя нет такого счета");
//                });
//
//
//        Map<String, Object> params = Map.of(
//                "userId", request.getUserId(),
//                "currency", request.getToCurrency()
//        );
//        Long toAccountId;
//        try {
//            toAccountId = restTemplate.exchange("http://account/accounts/findAccountId?userId={userId}&currency={currency}",
//                            HttpMethod.GET,
//                            entity,
//                            Long.class,
//                            params)
//                    .getBody();
//        } catch (Exception e) {
//            notificationService.send("У получателя нет счета с такой валютой");
//            throw new BadRequestException("У пользователя нет счета с такой валютой");
//        }
//
//        params = Map.of(
//                "from", fromCurrency,
//                "to", request.getToCurrency(),
//                "amount", request.getAmount()
//        );
//
//        Double toAmount = getConvertedAmount(fromCurrency, request.getToCurrency(), request.getAmount());
////        try {
////            ExchangeResponse rateInfo = restTemplate.exchange("http://exchange/rate/convert?from={from}&to={to}&amount={amount}",
////                    HttpMethod.GET,
////                    null,
////                    ExchangeResponse.class,
////                    params).getBody();
////            toAmount = rateInfo.getValue();
////        } catch (Exception e) {
////            notificationService.send("Ошибка перевода.");
////            throw new InternalServerErrorException();
////        }
//
//        return transfer(request.getFromAccountId(),
//                toAccountId,
//                request.getAmount(),
//                toAmount,
//                token);
//    }


}
