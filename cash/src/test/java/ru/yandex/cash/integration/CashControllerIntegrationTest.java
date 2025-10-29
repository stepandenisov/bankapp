package ru.yandex.cash.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.cash.model.CashRequest;
import ru.yandex.cash.service.CashService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CashControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashService cashService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void withdraw_success() throws Exception {
        when(cashService.withdraw(eq(1L), any(CashRequest.class))).thenReturn(true);

        CashRequest request = new CashRequest();
        request.setAmount(1000.0);

        mockMvc.perform(post("/withdraw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cashService).withdraw(eq(1L), any(CashRequest.class));
    }

    @Test
    void withdraw_fail() throws Exception {
        when(cashService.withdraw(eq(1L), any(CashRequest.class))).thenReturn(false);

        CashRequest request = new CashRequest();
        request.setAmount(500.0);

        mockMvc.perform(post("/withdraw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topUp_success() throws Exception {
        when(cashService.topUp(eq(2L), any(CashRequest.class))).thenReturn(true);

        CashRequest request = new CashRequest();
        request.setAmount(1500.0);

        mockMvc.perform(post("/top-up/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cashService).topUp(eq(2L), any(CashRequest.class));
    }

    @Test
    void topUp_fail() throws Exception {
        when(cashService.topUp(eq(2L), any(CashRequest.class))).thenReturn(false);

        CashRequest request = new CashRequest();
        request.setAmount(700.0);

        mockMvc.perform(post("/top-up/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
