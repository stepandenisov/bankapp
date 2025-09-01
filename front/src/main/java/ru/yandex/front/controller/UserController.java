package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.service.UserService;

import java.time.LocalDate;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(path = "/password", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editPassword(@RequestPart String password, @RequestPart String confirmPassword) {
        userService.editPassword(new EditPasswordRequest(password, confirmPassword));
        return "redirect:/";
    }

    @PostMapping(path = "/info", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String editInfo(@RequestPart String name, @RequestPart LocalDate birthday) {
        userService.editInfo(new EditUserInfoRequest(name, birthday));
        return "redirect:/";
    }
}
