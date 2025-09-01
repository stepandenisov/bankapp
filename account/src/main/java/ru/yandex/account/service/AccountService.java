package ru.yandex.account.service;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public boolean existsByAccountType(Currency currency){
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

    public Long getAccountIdByCurrencyAndUserId(Currency currency, Long userId){
        return accountRepository.findByUserIdAndCurrency(userId, currency)
                .map(Account::getId)
                .orElseThrow(NotFoundException::new);
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
                        return true;
                    }
                    return false;
                })
                .orElseThrow(() -> new AccessDeniedException("Нет доступа"));
    }

//    public boolean topUpForUser(Long accountId, Double amount) throws AccessDeniedException {
//        User user = userService.getCurrentUser();
//        return accountRepository.findByIdAndUser(accountId, user)
//                .map(account -> {
//                    account.setReminder(account.getReminder() + amount);
//                    accountRepository.save(account);
//                    return true;
//                })
//                .orElseThrow(() -> new AccessDeniedException("Нет доступа"));
//    }

    public boolean topUp(Long accountId, Double amount) throws AccessDeniedException {
        return accountRepository.findById(accountId)
                .map(account -> {
                    account.setReminder(account.getReminder() + amount);
                    accountRepository.save(account);
                    return true;
                })
                .orElseThrow(() -> new AccessDeniedException("Нет доступа"));
    }

//    public boolean topUpForUser(Long userId, Currency accountType, Double amount) throws AccessDeniedException {
//        User user = userService.findUserById(userId);
//        return accountRepository.findByIdAndUser(accountType, user)
//                .map(account -> {
//                    account.setReminder(account.getReminder() + amount);
//                    accountRepository.save(account);
//                    return true;
//                })
//                .orElseThrow(() -> new AccessDeniedException("Нет счета."));
//    }
}
