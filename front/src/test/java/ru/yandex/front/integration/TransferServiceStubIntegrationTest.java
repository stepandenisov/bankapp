package ru.yandex.front.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.front.model.Currency;
import ru.yandex.front.model.ExternalTransferRequest;
import ru.yandex.front.model.SelfTransferRequest;
import ru.yandex.front.service.TransferService;

public class TransferServiceStubIntegrationTest extends BaseServiceStubIntegrationTest{

    @Autowired
    private TransferService transferService;

    @Test
    void transferService_selfTransfer_Success() {
        SelfTransferRequest request = new SelfTransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(50.0);

        transferService.selfTransfer(request);
    }

    @Test
    void transferService_externalTransfer_Success() {
        ExternalTransferRequest request = new ExternalTransferRequest();
        request.setUserId(1L);
        request.setFromAccountId(1L);
        request.setToCurrency(Currency.CNY);
        request.setAmount(100.0);

        transferService.externalTransfer(request);
    }
}
