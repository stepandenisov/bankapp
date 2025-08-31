package ru.yandex.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.cash.model.CashRequest;
import ru.yandex.cash.model.RequestSpecificInfo;
import ru.yandex.cash.service.CashService;

@RestController
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/withdraw/{id}")
    public ResponseEntity<?> withdraw(@PathVariable(name = "id") Long id,
                                      @RequestBody CashRequest cashRequest,
                                      Authentication authentication){
        RequestSpecificInfo requestSpecificInfo = new RequestSpecificInfo(id,
                cashRequest,
                authentication.getCredentials().toString());
        if (cashService.withdraw(requestSpecificInfo)){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/top-up/{id}")
    public ResponseEntity<?> topUp(@PathVariable(name = "id") Long id,
                                   @RequestBody CashRequest cashRequest,
                                   Authentication authentication) {
        RequestSpecificInfo requestSpecificInfo = new RequestSpecificInfo(id,
                cashRequest,
                authentication.getCredentials().toString());
        if (cashService.topUp(requestSpecificInfo)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
