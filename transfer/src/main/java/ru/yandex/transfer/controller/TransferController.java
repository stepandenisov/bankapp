package ru.yandex.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.transfer.model.ExternalTransferRequest;
import ru.yandex.transfer.model.SelfTransferRequest;
import ru.yandex.transfer.model.TransferRequestDto;
import ru.yandex.transfer.service.TransferService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/external")
    public ResponseEntity<?> externalTransfer(@RequestBody ExternalTransferRequest request) throws BadRequestException {
        if (transferService.externalTransfer(request)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
    }

    @PostMapping("/self")
    public ResponseEntity<?> selfTransfer(@RequestBody SelfTransferRequest request) {
        if (transferService.selfTransfer(request)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
    }

}
