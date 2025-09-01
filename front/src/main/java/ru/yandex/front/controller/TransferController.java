package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.front.model.ExternalTransferRequest;
import ru.yandex.front.model.SelfTransferRequest;
import ru.yandex.front.service.TransferService;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping(value = "/self", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String selfTransfer(@ModelAttribute SelfTransferRequest selfTransferRequest){
        transferService.selfTransfer(selfTransferRequest);
        return "redirect:/";
    }

    @PostMapping("/external")
    public String externalTransfer(@ModelAttribute ExternalTransferRequest externalTransferRequest){
        transferService.externalTransfer(externalTransferRequest);
        return "redirect:/";
    }
}
