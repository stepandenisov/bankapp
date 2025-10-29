package ru.yandex.front.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.front.controller.AccountController;
import ru.yandex.front.model.Currency;
import ru.yandex.front.service.AccountService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

class AccountControllerUnitTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    void addAccount_shouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("currency", "RUB"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).addAccount(Currency.RUB);
    }

    @Test
    void deleteAccount_shouldCallServiceAndRedirect() throws Exception {
        Long accountId = 5L;

        mockMvc.perform(post("/accounts/{id}/delete", accountId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).deleteAccount(accountId);
    }
}
