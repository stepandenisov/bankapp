package ru.yandex.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.transfer.model.TransferRequest;
import ru.yandex.transfer.model.TransferRequestDto;
import ru.yandex.transfer.service.TransferService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        TransferRequestDto requestDto = new TransferRequestDto(request, authentication.getCredentials().toString());
        if (transferService.transfer(requestDto)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
    }

}
