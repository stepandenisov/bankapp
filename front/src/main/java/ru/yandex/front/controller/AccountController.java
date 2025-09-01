package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.front.model.Currency;
import ru.yandex.front.service.AccountService;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping(path = {"", "/"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String addAccount(@RequestParam("currency") Currency currency){
        accountService.addAccount(currency);
        return "redirect:/";
    }

    @PostMapping(path = "/{id}/delete")
    public String addAccount(@PathVariable("id") Long id){
        accountService.deleteAccount(id);
        return "redirect:/";
    }

}
