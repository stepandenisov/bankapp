package ru.yandex.blocker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.blocker.service.BlockerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class BlockerController {

    private final BlockerService blockerService;

    @GetMapping
    private ResponseEntity<Boolean> checkSuspicious(){
        return ResponseEntity.ok(blockerService.isSuspicious());
    }

}
