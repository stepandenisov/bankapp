package ru.yandex.account.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.integration.BaseIntegrationTest;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.dto.AccountDto;
import ru.yandex.account.model.dto.CashRequest;
import ru.yandex.account.service.AccountService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "testuser")
class AccountControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;



    @Test
    void addAccount_shouldCreateAccount() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts).extracting(Account::getCurrency).contains(Currency.USD);
    }

    @Test
    void addAccount_duplicateShouldReturnBadRequest() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Валютный счет (USD) уже существует."));
    }

    @Test
    void findAccounts_shouldReturnList() throws Exception {
        accountService.save(new AccountDto(Currency.USD));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }

    @Test
    void findAccountId_shouldReturnAccountId() throws Exception {
        AccountDto dto = new AccountDto(Currency.CNY);
        accountService.save(dto);

        Long id = accountService.getAccountIdByCurrencyAndUserId(Currency.CNY, testUser.getId());

        mockMvc.perform(get("/accounts/findAccountId")
                        .param("userId", testUser.getId().toString())
                        .param("currency", "CNY"))
                .andExpect(status().isOk())
                .andExpect(content().string(id.toString()));
    }

    @Test
    void topUp_shouldIncreaseBalance() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);
        Long id = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());

        CashRequest request = new CashRequest(200.0);

        mockMvc.perform(post("/accounts/{id}/top-up", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Account acc = accountRepository.findById(id).orElseThrow();
        assertThat(acc.getReminder()).isEqualTo(200.0);
    }

    @Test
    void withdraw_shouldDecreaseBalance() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);
        Long id = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());

        accountService.topUp(id, 300.0);

        CashRequest withdraw = new CashRequest(100.0);

        mockMvc.perform(post("/accounts/{id}/withdraw", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isOk());

        Account acc = accountRepository.findById(id).orElseThrow();
        assertThat(acc.getReminder()).isEqualTo(200.0);

    }

    @Test
    void deleteAccount_withZeroBalance_shouldReturnOk() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);
        Long id = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());

        mockMvc.perform(delete("/accounts/{id}", id))
                .andExpect(status().isOk());



        assertThat(accountRepository.findById(id)).isEqualTo(Optional.empty());
    }

    @Test
    void deleteAccount_withNonZeroBalance_shouldReturnBadRequest() throws Exception {
        AccountDto dto = new AccountDto(Currency.USD);
        accountService.save(dto);
        Long id = accountService.getAccountIdByCurrencyAndUserId(Currency.USD, testUser.getId());
        accountService.topUp(id, 100.0);

        mockMvc.perform(delete("/accounts/{id}", id))
                .andExpect(status().isBadRequest());

        assertThat(accountRepository.findById(id).isPresent()).isEqualTo(true);
    }
}