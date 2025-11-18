package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.front.model.Account;
import ru.yandex.front.model.Currency;
import ru.yandex.front.service.AccountService;
import ru.yandex.front.service.CurrencyService;
import ru.yandex.front.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FrontController {

    private final AccountService accountService;
    private final CurrencyService currencyService;

    private final UserService userService;

    @Value("${currency.uri}")
    private String currencyUri;


    @GetMapping(path = {"", "/"})
    public String main(Model model) {
        List<Account> accounts = accountService.getAccounts();
        List<Currency> currencies = currencyService.getCurrencies();
        List<Currency> userCurrencies = accounts.stream().map(Account::getCurrency).toList();
        List<Currency> availableCurrencies = new ArrayList<>(currencies);
        availableCurrencies.removeAll(userCurrencies);
        model.addAttribute("accounts", accounts);
        model.addAttribute("availableCurrencies", availableCurrencies);
        model.addAttribute("currency", currencies);
        model.addAttribute("userCurrency", userCurrencies);
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("currency.uri", currencyUri);
        return "main";
    }

    @GetMapping("/register")
    public String signup() {
        return "signup";
    }

}
