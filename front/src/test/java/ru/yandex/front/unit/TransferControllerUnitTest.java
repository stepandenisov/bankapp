package ru.yandex.front.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.front.controller.TransferController;
import ru.yandex.front.model.ExternalTransferRequest;
import ru.yandex.front.model.SelfTransferRequest;
import ru.yandex.front.service.TransferService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerUnitTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
    }

    @Test
    void selfTransfer_shouldCallServiceAndRedirect() throws Exception {
        SelfTransferRequest request = new SelfTransferRequest();
        mockMvc.perform(post("/transfer/self")
                        .contentType("multipart/form-data"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(transferService, times(1)).selfTransfer(any(SelfTransferRequest.class));
    }

    @Test
    void externalTransfer_shouldCallServiceAndRedirect() throws Exception {
        ExternalTransferRequest request = new ExternalTransferRequest();
        mockMvc.perform(post("/transfer/external")
                        .contentType("multipart/form-data"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(transferService, times(1)).externalTransfer(any(ExternalTransferRequest.class));
    }
}
