package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.model.RegisterRequest;
import ru.yandex.front.service.UserService;

import java.time.LocalDate;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(path = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String register(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("confirmPassword") String confirmPassword,
                         @RequestParam("fullName") String fullName,
                         @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday){
        userService.register(new RegisterRequest(username, password, confirmPassword, fullName, birthday));
        return "redirect:/";
    }

    @PostMapping(path = "/password", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editPassword(@RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword) {
        userService.editPassword(new EditPasswordRequest(password, confirmPassword));

        return "redirect:/";
    }

    @PostMapping(path = "/info", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editInfo(@RequestParam("name") String name, @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday) {
        userService.editInfo(new EditUserInfoRequest(name, birthday));
        return "redirect:/";
    }
}
