package ru.yandex.account.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.AccountDto;
import ru.yandex.account.service.AccountService;
import ru.yandex.account.service.UserService;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceUnitTest {

    private AccountRepository accountRepository;
    private UserService userService;
    private AccountService accountService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        userService = mock(UserService.class);
        accountService = new AccountService(accountRepository, userService);

        currentUser = new User(1L, "user", "pass", "Full Name", null, "USER", null);
        when(userService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void testExistsByAccountType() {
        when(accountRepository.existsByUserIdAndCurrency(currentUser.getId(), Currency.USD)).thenReturn(true);

        boolean exists = accountService.existsByAccountType(Currency.USD);

        assertTrue(exists);
        verify(accountRepository, times(1)).existsByUserIdAndCurrency(currentUser.getId(), Currency.USD);
    }

    @Test
    void testDelete_Success() throws AccessDeniedException {
        Account account = new Account(1L, Currency.USD, 0.0, currentUser);
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(account));

        boolean result = accountService.delete(1L);

        assertTrue(result);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void testDelete_Failure_ReminderNotZero() throws AccessDeniedException {
        Account account = new Account(1L, Currency.USD, 100.0, currentUser);
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(account));

        boolean result = accountService.delete(1L);

        assertFalse(result);
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void testDelete_NoAccess() {
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> accountService.delete(1L));
    }

    @Test
    void testSave() {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account savedAccount = captor.getValue();
        assertEquals(Currency.USD, savedAccount.getCurrency());
        assertEquals(0.0, savedAccount.getReminder());
        assertEquals(currentUser, savedAccount.getUser());
    }

    @Test
    void testGetAccountIdByCurrencyAndUserId_Success() {
        Account account = new Account(1L, Currency.USD, 0.0, currentUser);
        when(accountRepository.findByUserIdAndCurrency(1L, Currency.USD)).thenReturn(Optional.of(account));

        Long accountId = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, 1L);

        assertEquals(1L, accountId);
    }

    @Test
    void testGetAccountIdByCurrencyAndUserId_NotFound() {
        when(accountRepository.findByUserIdAndCurrency(1L, Currency.USD)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> accountService.getAccountIdByCurrencyAndUserId(Currency.USD, 1L));
    }

    @Test
    void testFindAccountsDtoOfCurrentUser() {
        List<Account> accounts = List.of(
                new Account(1L, Currency.USD, 100.0, currentUser),
                new Account(2L, Currency.CNY, 50.0, currentUser)
        );
        when(accountRepository.findByUser(currentUser)).thenReturn(accounts);

        List<Account> result = accountService.findAccountsDtoOfCurrentUser();

        assertEquals(accounts, result);
    }

    @Test
    void testWithdraw_Success() throws AccessDeniedException {
        Account account = new Account(1L, Currency.USD, 100.0, currentUser);
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(account));

        boolean result = accountService.withdraw(1L, 50.0);

        assertTrue(result);
        assertEquals(50.0, account.getReminder());
        verify(accountRepository).save(account);
    }

    @Test
    void testWithdraw_Failure_NotEnoughMoney() throws AccessDeniedException {
        Account account = new Account(1L, Currency.USD, 30.0, currentUser);
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.of(account));

        boolean result = accountService.withdraw(1L, 50.0);

        assertFalse(result);
        assertEquals(30.0, account.getReminder());
        verify(accountRepository, never()).save(account);
    }

    @Test
    void testWithdraw_NoAccess() {
        when(accountRepository.findByIdAndUser(1L, currentUser)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> accountService.withdraw(1L, 50.0));
    }

    @Test
    void testTopUp_Success() throws AccessDeniedException {
        Account account = new Account(1L, Currency.USD, 100.0, currentUser);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        boolean result = accountService.topUp(1L, 50.0);

        assertTrue(result);
        assertEquals(150.0, account.getReminder());
        verify(accountRepository).save(account);
    }

    @Test
    void testTopUp_NoAccess() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> accountService.topUp(1L, 50.0));
    }
}