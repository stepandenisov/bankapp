package ru.yandex.transfer.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
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

    private final MeterRegistry registry;

    @Value("${account.uri}")
    String accountUri;

    @Value("${exchange.uri}")
    private String exchangeUri;

    public void recordFailedTransfer(String fromAcc, String toAcc) {

        registry.counter(
                "transfer_fail",
                "from_account", fromAcc,
                "to_account", toAcc
        ).increment();
    }

    public void recordSuspiciousTransfer(String fromAcc, String toAcc) {

        registry.counter(
                "transfer_suspicious",
                "from_account", fromAcc,
                "to_account", toAcc
        ).increment();
    }


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
                    accountUri + "/accounts/" + fromAccountId + "/withdraw",
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
                    accountUri + "/accounts/" + toAccountId + "/top-up",
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
            recordFailedTransfer(fromAccountId.toString(), toAccountId.toString());
            notificationService.send("Ошибка перевода.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "exchange failed");
        }
        try {
            topUpDecorated.get();
        } catch (Exception e) {
            recordFailedTransfer(fromAccountId.toString(), toAccountId.toString());
            Supplier<Boolean> revertTopUpSupplier = () -> {
                HttpHeaders headers = new HttpHeaders();
//                headers.set("Authorization", "Bearer " + token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                restTemplate.exchange(
                        accountUri + "/accounts/" + fromAccountId + "/top-up",
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "exchange failed");
            } catch (Exception e2) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "exchange failed");
            }
        }
        notificationService.send("Перевод осуществлен.");
        return true;
    }


    private Double getConvertedAmount(Currency fromCurrency,
                                      Currency toCurrency,
                                      Double amount,
                                      String fromAccountId,
                                      String toAccountId) {
        Supplier<Double> supplier = () -> {
            Map<String, Object> params = Map.of(
                    "from", fromCurrency,
                    "to", toCurrency,
                    "amount", amount
            );
            ExchangeResponse rateInfo = restTemplate.exchange(
                    exchangeUri + "/rate/convert?from={from}&to={to}&amount={amount}",
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
            recordFailedTransfer(fromAccountId, toAccountId);
            notificationService.send("Ошибка перевода.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "exchange failed");
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
                    accountUri + "/accounts",
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
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "exchange failed");
                });
    }

    public boolean selfTransfer(SelfTransferRequest request) {
        try {
            if (blockerService.checkSuspicious()) {
                recordSuspiciousTransfer(request.getFromAccountId().toString(), request.getToAccountId().toString());
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
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "exchange failed");
                    });

            Double toAmount = getConvertedAmount(fromCurrency, toCurrency, request.getAmount(),
                    request.getFromAccountId().toString(), request.getToAccountId().toString());

            return transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount(), toAmount, token);
        } catch (Exception ignored) {
            recordFailedTransfer(request.getFromAccountId().toString(), request.getToAccountId().toString());
            return false;
        }
    }

    public boolean externalTransfer(ExternalTransferRequest request) {
        try {
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
                                accountUri + "/accounts/findAccountId?userId={userId}&currency={currency}",
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exchange failed");
            }

            if (blockerService.checkSuspicious()) {
                recordSuspiciousTransfer(request.getFromAccountId().toString(), toAccountId.toString());
                notificationService.send("Подозрительная операция заблокирована.");
                return false;
            }

            Double toAmount = getConvertedAmount(fromCurrency, request.getToCurrency(), request.getAmount(),
                    request.getFromAccountId().toString(), toAccountId.toString());

            return transfer(request.getFromAccountId(), toAccountId, request.getAmount(), toAmount, token);
        } catch (Exception ignored) {
            recordFailedTransfer(request.getFromAccountId().toString(), request.getUserId().toString() + "_" + request.getToCurrency().toString());
            return false;
        }
    }
}
