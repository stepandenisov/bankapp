package ru.yandex.front.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.front.service.CashService;

public class CashServiceStubIntegrationTest extends BaseServiceStubIntegrationTest{

    @Autowired
    private CashService cashService;

    @Test
    void cashService_withdraw_Success() {
        cashService.withdraw(123L, 500.0);
    }

    @Test
    void cashService_topUp_Success() {
        cashService.topUp(123L, 1000.0);
    }
}
