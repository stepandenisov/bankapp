package ru.yandex.front.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.front.model.Account;
import ru.yandex.front.model.Currency;
import ru.yandex.front.service.AccountService;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountServiceStubIntegrationTest extends BaseServiceStubIntegrationTest{

    @Autowired
    private AccountService accountService;

    @Test
    void accountService_getAccounts_ReturnsList() {
        List<Account> accounts = accountService.getAccounts();
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        assertTrue(accounts.stream().anyMatch(acc -> acc.getCurrency() != null));
    }

    @Test
    void accountService_addAccount_Success() {
        accountService.addAccount(Currency.USD);
    }

    @Test
    void accountService_deleteAccount_Success() {
        accountService.deleteAccount(1L);
    }

}
