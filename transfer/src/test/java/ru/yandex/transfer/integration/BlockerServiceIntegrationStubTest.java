package ru.yandex.transfer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.transfer.service.BlockerService;

import static org.junit.jupiter.api.Assertions.*;

class BlockerServiceIntegrationStubTest extends BaseServiceStubIntegrationTest {

    @Autowired
    private BlockerService blockerService;

    @Test
    void checkSuspicious_ShouldReturnBooleanFromStub() {
        boolean result = blockerService.checkSuspicious();
        assertNotNull(result);
    }
}
