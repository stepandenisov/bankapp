package ru.yandex.front.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.front.controller.CashController;
import ru.yandex.front.service.CashService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CashControllerUnitTest {

    @Mock
    private CashService cashService;

    @InjectMocks
    private CashController cashController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cashController).build();
    }

    @Test
    void withdraw_shouldCallWithdrawAndRedirect() throws Exception {
        Long accountId = 1L;
        Double volume = 100.0;

        mockMvc.perform(post("/cash")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("accountId", accountId.toString())
                        .param("volume", volume.toString())
                        .param("action", "GET"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cashService, times(1)).withdraw(accountId, volume);
        verify(cashService, never()).topUp(anyLong(), anyDouble());
    }

    @Test
    void withdraw_shouldCallTopUpAndRedirect() throws Exception {
        Long accountId = 2L;
        Double volume = 50.0;

        mockMvc.perform(post("/cash")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("accountId", accountId.toString())
                        .param("volume", volume.toString())
                        .param("action", "PUT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cashService, times(1)).topUp(accountId, volume);
        verify(cashService, never()).withdraw(anyLong(), anyDouble());
    }
}
