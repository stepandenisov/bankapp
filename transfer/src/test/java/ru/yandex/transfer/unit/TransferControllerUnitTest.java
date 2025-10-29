package ru.yandex.transfer.unit;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.transfer.controller.TransferController;
import ru.yandex.transfer.model.Currency;
import ru.yandex.transfer.model.ExternalTransferRequest;
import ru.yandex.transfer.model.SelfTransferRequest;
import ru.yandex.transfer.service.TransferService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransferControllerUnitTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private ExternalTransferRequest externalRequest;
    private SelfTransferRequest selfRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        externalRequest = new ExternalTransferRequest();
        externalRequest.setAmount(100.0);
        externalRequest.setFromAccountId(1L);
        externalRequest.setToCurrency(Currency.RUB);

        selfRequest = new SelfTransferRequest();
        selfRequest.setAmount(50.0);
        selfRequest.setFromAccountId(1L);
        selfRequest.setToAccountId(2L);
    }

    @Test
    void externalTransfer_shouldReturnOk_whenServiceReturnsTrue() throws BadRequestException {
        when(transferService.externalTransfer(externalRequest)).thenReturn(true);

        ResponseEntity<?> response = transferController.externalTransfer(externalRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transferService).externalTransfer(externalRequest);
    }

    @Test
    void externalTransfer_shouldReturnForbidden_whenServiceReturnsFalse() throws BadRequestException {
        when(transferService.externalTransfer(externalRequest)).thenReturn(false);

        ResponseEntity<?> response = transferController.externalTransfer(externalRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(transferService).externalTransfer(externalRequest);
    }

    @Test
    void selfTransfer_shouldReturnOk_whenServiceReturnsTrue() {
        when(transferService.selfTransfer(selfRequest)).thenReturn(true);

        ResponseEntity<?> response = transferController.selfTransfer(selfRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transferService).selfTransfer(selfRequest);
    }

    @Test
    void selfTransfer_shouldReturnForbidden_whenServiceReturnsFalse() {
        when(transferService.selfTransfer(selfRequest)).thenReturn(false);

        ResponseEntity<?> response = transferController.selfTransfer(selfRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(transferService).selfTransfer(selfRequest);
    }
}
