package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.controller.dto.request.PlaceBetRequest;
import com.sportygroup.f1betting.controller.dto.response.BetResponseDto;
import com.sportygroup.f1betting.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bets")
@RequiredArgsConstructor
public class BetController {
    private final BetService betService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponseDto placeBet(@RequestBody PlaceBetRequest request) {
        return betService.placeBet(request.userId(), request.eventOddId(), request.amount());
    }

}
