package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.front.model.Action;
import ru.yandex.front.model.CashRequest;
import ru.yandex.front.service.CashService;

@Controller
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping(value = {"/", ""}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String withdraw(@RequestParam Long accountId, @RequestParam Double volume, @RequestParam Action action){
        switch (action){
            case PUT -> cashService.topUp(accountId, volume);
            case GET -> cashService.withdraw(accountId, volume);
        }
        return "redirect:/";
    }

}
