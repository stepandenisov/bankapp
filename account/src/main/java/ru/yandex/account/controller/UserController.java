package ru.yandex.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.account.model.dto.EditPasswordRequest;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.model.dto.UserDto;
import ru.yandex.account.service.UserService;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<UserDto> getUserInfo(){
        return ResponseEntity.ok(userService.getCurrentUserDto());
    }

    @DeleteMapping(path = {"/", ""})
    public ResponseEntity<?> deleteUser(){
        userService.deleteCurrentUser();
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = {"/password"})
    public ResponseEntity<?> editPassword(@RequestBody EditPasswordRequest request){
        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Пароли не совпадают");
        }
        userService.editPassword(request.getPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = {"/info"})
    public ResponseEntity<?> editUserInfo(@RequestBody EditUserInfoRequest request){
        userService.editUser(request);
        return ResponseEntity.ok().build();
    }
}
