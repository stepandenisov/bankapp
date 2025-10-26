package ru.yandex.account.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.integration.BaseIntegrationTest;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.AccountDto;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.service.AccountService;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "testuser")
class AccountServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldCreateAccountForCurrentUser() {
        AccountDto dto = new AccountDto(Currency.USD);

        accountService.save(dto);

        List<Account> accounts = accountRepository.findByUser(testUser);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getCurrency()).isEqualTo(Currency.USD);
        assertThat(accounts.get(0).getReminder()).isEqualTo(0.0);
    }

    @Test
    void existsByAccountType_shouldReturnTrue_whenAccountExists() {
        accountService.save(new AccountDto(Currency.USD));

        boolean exists = accountService.existsByAccountType(Currency.USD);
        assertThat(exists).isTrue();
    }

    @Test
    void topUp_shouldIncreaseBalance() throws AccessDeniedException {
        accountService.save(new AccountDto(Currency.USD));
        Long accountId = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());

        boolean result = accountService.topUp(accountId, 200.0);

        assertThat(result).isTrue();
        Account acc = accountRepository.findById(accountId).get();
        assertThat(acc.getReminder()).isEqualTo(200.0);
    }

    @Test
    void withdraw_shouldDecreaseBalance() throws AccessDeniedException {
        accountService.save(new AccountDto(Currency.USD));
        Long accountId = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());
        accountService.topUp(accountId, 300.0);

        boolean result = accountService.withdraw(accountId, 100.0);

        assertThat(result).isTrue();
        Account acc = accountRepository.findById(accountId).get();
        assertThat(acc.getReminder()).isEqualTo(200.0);
    }

    @Test
    void withdraw_shouldThrow_whenAccountNotBelongsToUser() {
        User other = new User();
        other.setUsername("other");
        other.setPassword("password");
        other.setRoles("USER");
        userRepository.save(other);

        Account acc = new Account(null, Currency.USD, 500.0, other);
        accountRepository.save(acc);

        assertThatThrownBy(() -> accountService.withdraw(acc.getId(), 100.0))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_shouldRemoveAccount_whenBalanceZero() throws AccessDeniedException {
        accountService.save(new AccountDto(Currency.USD));
        Long accountId = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());

        boolean deleted = accountService.delete(accountId);

        assertThat(deleted).isTrue();
        assertThat(accountRepository.findById(accountId)).isEmpty();
    }

    @Test
    void delete_shouldReturnFalse_whenBalanceNotZero() throws AccessDeniedException {
        accountService.save(new AccountDto(Currency.USD));
        Long accountId = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());
        accountService.topUp(accountId, 100.0);

        boolean deleted = accountService.delete(accountId);

        assertThat(deleted).isFalse();
        assertThat(accountRepository.findById(accountId)).isPresent();
    }

    @Test
    void findAccountsDtoOfCurrentUser_shouldReturnOnlyUsersAccounts() {
        accountService.save(new AccountDto(Currency.USD));

        User other = new User();
        other.setUsername("other");
        other.setPassword("pass");
        other.setRoles("USER");
        userRepository.save(other);

        Account foreign = new Account(null, Currency.USD, 0.0, other);
        accountRepository.save(foreign);

        List<Account> result = accountService.findAccountsDtoOfCurrentUser();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("testuser");
    }
}
