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
    public String register(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam String fullName,
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday){
        userService.register(new RegisterRequest(username, password, confirmPassword, fullName, birthday));
        return "redirect:/";
    }

    @PostMapping(path = "/password", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editPassword(@RequestPart String password, @RequestPart String confirmPassword) {
        userService.editPassword(new EditPasswordRequest(password, confirmPassword));

        return "redirect:/";
    }

    @PostMapping(path = "/info", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editInfo(@RequestParam String name, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday) {
        userService.editInfo(new EditUserInfoRequest(name, birthday));
        return "redirect:/";
    }
}
