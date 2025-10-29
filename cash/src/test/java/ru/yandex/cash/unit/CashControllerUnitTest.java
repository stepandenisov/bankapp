package ru.yandex.cash.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.cash.configuration.TestSecurityConfig;
import ru.yandex.cash.controller.CashController;
import ru.yandex.cash.model.CashRequest;
import ru.yandex.cash.service.CashService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashController.class)
@Import(TestSecurityConfig.class)
class CashControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CashService cashService;

    @Test
    void withdraw_shouldReturnOk() throws Exception {
        Long id = 1L;
        CashRequest req = new CashRequest();
        when(cashService.withdraw(eq(id), any(CashRequest.class))).thenReturn(true);

        mockMvc.perform(post("/withdraw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void withdraw_shouldReturnBadRequest() throws Exception {
        Long id = 1L;
        CashRequest req = new CashRequest();
        when(cashService.withdraw(id, req)).thenReturn(false);

        mockMvc.perform(post("/withdraw/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topUp_shouldReturnOk() throws Exception {
        Long id = 2L;
        CashRequest req = new CashRequest();
        when(cashService.topUp(eq(id), any(CashRequest.class))).thenReturn(true);

        mockMvc.perform(post("/top-up/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void topUp_shouldReturnBadRequest() throws Exception {
        Long id = 2L;
        CashRequest req = new CashRequest();
        when(cashService.topUp(id, req)).thenReturn(false);

        mockMvc.perform(post("/top-up/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
