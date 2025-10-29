package ru.yandex.transfer.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.yandex.transfer.model.SelfTransferRequest;
import ru.yandex.transfer.service.TransferService;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferServiceIntegrationStubTest extends BaseServiceStubIntegrationTest {

    @Autowired
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public String getName() {
                return "test-user";
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getDetails() {
                return "dummy-token";
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
        });
    }

    @Test
    void selfTransfer_ShouldCompleteSuccessfully() {
        SelfTransferRequest selfTransferRequest = new SelfTransferRequest();
        selfTransferRequest.setFromAccountId(1L);
        selfTransferRequest.setToAccountId(2L);
        selfTransferRequest.setAmount(100.0);
        boolean result = transferService.selfTransfer(selfTransferRequest);
        assertTrue(result);
    }
}
