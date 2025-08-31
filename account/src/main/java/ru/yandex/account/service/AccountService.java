package ru.yandex.account.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.AccountType;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.AccountDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private final UserService userService;

    public boolean existsByAccountType(AccountType accountType){
        User user = userService.getCurrentUser();
        return accountRepository.existsByUserIdAndAccountType(user.getId(),accountType);
    }

    public void save(AccountDto accountDto) {
        Account account = new Account(
                null,
                accountDto.type(),
                accountDto.reminder(),
                userService.getCurrentUser()
        );
        accountRepository.save(account);
    }

    public AccountType getCurrencyOfAccountById(Long id){
        return accountRepository.findById(id)
                .map(Account::getAccountType)
                .orElseThrow(NotFoundException::new);
    }

    public List<AccountDto> findAccountsDtoOfCurrentUser() {
        return accountRepository.findByUser(userService.getCurrentUser())
                .stream()
                .map(account -> new AccountDto(account.getAccountType(), account.getReminder()))
                .toList();
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

//    public boolean topUpForUser(Long userId, AccountType accountType, Double amount) throws AccessDeniedException {
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
