package ru.yandex.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.dto.AccountDto;
import ru.yandex.account.model.dto.CashRequest;
import ru.yandex.account.service.AccountService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<Account>> findAccounts() {
        return ResponseEntity.ok(accountService.findAccountsDtoOfCurrentUser());
    }

    @GetMapping(path = "/findAccountId")
    public ResponseEntity<Long> accountId(@RequestParam(value = "userId") Long userId,
                                          @RequestParam("currency") Currency currency) {
        return ResponseEntity.ok(accountService.getAccountIdByCurrencyAndUserId(currency, userId));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> addAccount(@RequestBody AccountDto request) {
        if (accountService.existsByAccountType(request.type())) {
            return ResponseEntity.badRequest().body("Валютный счет (" + request.type() + ") уже существует.");
        }
        accountService.save(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable("id") Long accountId) throws AccessDeniedException {
        if (accountService.delete(accountId)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = {"/{id}/withdraw"})
    public ResponseEntity<?> withdraw(@PathVariable("id") Long id, @RequestBody CashRequest cashRequest) throws AccessDeniedException {
        if (accountService.withdraw(id, cashRequest.getAmount())) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(path = {"/{id}/top-up"})
    public ResponseEntity<?> topUp(@PathVariable("id") Long id, @RequestBody CashRequest cashRequest) throws AccessDeniedException {
        if (accountService.topUp(id, cashRequest.getAmount())) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

//    @PostMapping(path = {"/{id}/top-up"})
//    public ResponseEntity<?> topUpForUser(@PathVariable("type") Currency type, @PathVariable("id") Long id, @RequestBody CashRequest cashRequest) throws AccessDeniedException {
//        if (accountService.topUpForUser(id, type, cashRequest.getAmount())) {
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.badRequest().build();
//    }
}
