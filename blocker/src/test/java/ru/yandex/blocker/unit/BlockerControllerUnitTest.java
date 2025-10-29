package ru.yandex.blocker.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.blocker.controller.BlockerController;
import ru.yandex.blocker.service.BlockerService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockerController.class)
class BlockerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockerService blockerService;

    @Test
    @WithMockUser
    void shouldReturnTrueWhenSuspicious() throws Exception {
        when(blockerService.isSuspicious()).thenReturn(true);

        mockMvc.perform(get("/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser
    void shouldReturnFalseWhenNotSuspicious() throws Exception {
        when(blockerService.isSuspicious()).thenReturn(false);

        mockMvc.perform(get("/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
