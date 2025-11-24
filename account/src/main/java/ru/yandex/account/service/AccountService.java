package ru.yandex.account.service;

import io.micrometer.tracing.Tracer;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.AccountDto;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final Tracer tracer;

    public boolean existsByAccountType(Currency currency) {
        User user = userService.getCurrentUser();
        return accountRepository.existsByUserIdAndCurrency(user.getId(), currency);
    }

    public boolean delete(Long accountId) throws AccessDeniedException {
        User user = userService.getCurrentUser();
        Optional<Account> account = accountRepository.findByIdAndUser(accountId, user);
        if (account.isPresent()) {
            if (account.get().getReminder() == 0.0) {
                accountRepository.delete(account.get());
                return true;
            } else {
                return false;
            }
        } else {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.warn("Attempt to delete someone else's account: user: " + user.getUsername() + ", accountId: " + accountId.toString());
            ThreadContext.clearAll();
            throw new AccessDeniedException("Нет прав на удаление аккаунта с id " + accountId);
        }
    }

    public void save(AccountDto accountDto) {
        Account account = new Account(
                null,
                accountDto.type(),
                0.0,
                userService.getCurrentUser()
        );
        accountRepository.save(account);
    }

    public Long getAccountIdByCurrencyAndUserId(Currency currency, Long userId) {
        return accountRepository.findByUserIdAndCurrency(userId, currency)
                .map(Account::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    public List<Account> findAccountsDtoOfCurrentUser() {
        return accountRepository.findByUser(userService.getCurrentUser());
    }

    public boolean withdraw(Long accountId, Double amount) throws AccessDeniedException {
        User user = userService.getCurrentUser();
        return accountRepository.findByIdAndUser(accountId, user)
                .map(account -> {
                    if (account.getReminder() >= amount) {
                        account.setReminder(account.getReminder() - amount);
                        accountRepository.save(account);
                        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                        log.debug("Withdraw success.");
                        ThreadContext.clearAll();
                        return true;
                    }
                    return false;
                })
                .orElseThrow(() -> {
                    ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                    ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                    log.warn("Attempt to withdraw from else's account: user: " + user.getUsername() + ", accountId: " + accountId.toString());
                    ThreadContext.clearAll();
                    return new AccessDeniedException("Нет доступа");
                });
    }

    public boolean topUp(Long accountId, Double amount) throws AccessDeniedException {
        return accountRepository.findById(accountId)
                .map(account -> {
                    account.setReminder(account.getReminder() + amount);
                    accountRepository.save(account);
                    ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                    ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                    log.debug("Top up success.");
                    ThreadContext.clearAll();
                    return true;
                })
                .orElseThrow(() -> new AccessDeniedException("Нет доступа"));
    }
}
