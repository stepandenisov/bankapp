package ru.yandex.account.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.account.controller.AccountController;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.dto.AccountDto;
import ru.yandex.account.model.dto.CashRequest;
import ru.yandex.account.service.AccountService;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerUnitTest {

    private AccountService accountService;
    private AccountController accountController;

    @BeforeEach
    void setup() {
        accountService = mock(AccountService.class);
        accountController = new AccountController(accountService);
    }

    @Test
    void testFindAccounts() {
        List<Account> mockAccounts = List.of(new Account(), new Account());
        when(accountService.findAccountsDtoOfCurrentUser()).thenReturn(mockAccounts);

        ResponseEntity<List<Account>> response = accountController.findAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testAccountId() {
        when(accountService.getAccountIdByCurrencyAndUserId(Currency.USD, 1L)).thenReturn(42L);

        ResponseEntity<Long> response = accountController.accountId(1L, Currency.USD);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(42L, response.getBody());
    }

    @Test
    void testAddAccount_Success() {
        AccountDto dto = new AccountDto(Currency.USD);
        when(accountService.existsByAccountType(dto.type())).thenReturn(false);

        ResponseEntity<?> response = accountController.addAccount(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).save(dto);
    }

    @Test
    void testAddAccount_AlreadyExists() {
        AccountDto dto = new AccountDto(Currency.USD);
        when(accountService.existsByAccountType(dto.type())).thenReturn(true);

        ResponseEntity<?> response = accountController.addAccount(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("уже существует"));
        verify(accountService, never()).save(dto);
    }

    @Test
    void testDeleteAccount_Success() throws AccessDeniedException {
        when(accountService.delete(1L)).thenReturn(true);

        ResponseEntity<?> response = accountController.deleteAccount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteAccount_Failure() throws AccessDeniedException {
        when(accountService.delete(1L)).thenReturn(false);

        ResponseEntity<?> response = accountController.deleteAccount(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testWithdraw_Success() throws AccessDeniedException {
        CashRequest request = new CashRequest(100.0);
        when(accountService.withdraw(1L, 100.0)).thenReturn(true);

        ResponseEntity<?> response = accountController.withdraw(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testWithdraw_Failure() throws AccessDeniedException {
        CashRequest request = new CashRequest(100.0);
        when(accountService.withdraw(1L, 100.0)).thenReturn(false);

        ResponseEntity<?> response = accountController.withdraw(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testTopUp_Success() throws AccessDeniedException {
        CashRequest request = new CashRequest(200.0);
        when(accountService.topUp(1L, 200.0)).thenReturn(true);

        ResponseEntity<?> response = accountController.topUp(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testTopUp_Failure() throws AccessDeniedException {
        CashRequest request = new CashRequest(200.0);
        when(accountService.topUp(1L, 200.0)).thenReturn(false);

        ResponseEntity<?> response = accountController.topUp(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
