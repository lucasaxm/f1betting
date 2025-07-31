package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.BetResponseDto;
import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exception.DuplicateBetException;
import com.sportygroup.f1betting.exception.EventClosedException;
import com.sportygroup.f1betting.exception.EventOddNotFoundException;
import com.sportygroup.f1betting.exception.InsufficientBalanceException;
import com.sportygroup.f1betting.exception.InvalidBetAmountException;
import com.sportygroup.f1betting.exception.OutdatedEventOddException;
import com.sportygroup.f1betting.exception.UserNotFoundException;
import com.sportygroup.f1betting.repository.BetRepository;
import com.sportygroup.f1betting.repository.CurrentEventOddsViewRepository;
import com.sportygroup.f1betting.repository.EventOddRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BetService {
    private final UserRepository userRepository;
    private final EventOddRepository eventOddRepository;
    private final EventRepository eventRepository;
    private final CurrentEventOddsViewRepository oddsViewRepository;
    private final BetRepository betRepository;

    @Transactional
    public BetResponseDto placeBet(UUID userId, UUID eventOddId, BigDecimal amount) {
        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(UserNotFoundException::new);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBetAmountException();
        }

        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        EventOdd eventOdd = eventOddRepository.findById(eventOddId)
            .orElseThrow(EventOddNotFoundException::new);

        if (!oddsViewRepository.existsById(eventOddId)) {
            throw new OutdatedEventOddException();
        }

        if (betRepository.findByUserIdAndEventOddsId(userId, eventOddId).isPresent()) {
            throw new DuplicateBetException();
        }

        Event event = eventRepository.findByIdForUpdate(eventOdd.getEvent().getId())
            .orElseThrow(EventOddNotFoundException::new);
        if (event.getWinnerDriverId() != null) {
            throw new EventClosedException();
        }

        Bet bet = new Bet();
        bet.setUser(user);
        bet.setEvent(event);
        bet.setEventOdds(eventOdd);
        bet.setAmount(amount);
        bet.setStatus("PENDING");
        bet.setCreatedAt(OffsetDateTime.now());
        betRepository.save(bet);

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        return new BetResponseDto(bet, user.getBalance());
    }

}
