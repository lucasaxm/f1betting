package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.EventOutcomeResponseDto;
import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.BetStatus;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exception.*;
import com.sportygroup.f1betting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventOutcomeService {

    private final EventRepository eventRepository;
    private final DriverRepository driverRepository;
    private final EventOddRepository eventOddRepository;
    private final BetRepository betRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventOutcomeResponseDto declareOutcome(UUID eventId, UUID winnerDriverId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(EventNotFoundException::new);

        if (event.getWinnerDriverId() != null) {
            throw new EventAlreadyClosedException();
        }

        driverRepository.findById(winnerDriverId)
                .orElseThrow(DriverNotFoundException::new);

        if (!eventOddRepository.existsByEventIdAndDriverId(eventId, winnerDriverId)) {
            throw new DriverNotInEventException();
        }

        event.setWinnerDriverId(winnerDriverId);
        eventRepository.save(event);

        List<Bet> bets = betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING.name());
        int settled = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Bet bet : bets) {
            settled++;
            if (bet.getEventOdds().getDriver().getId().equals(winnerDriverId)) {
                bet.setStatus(BetStatus.WON.name());
                BigDecimal payout = bet.getAmount()
                        .multiply(BigDecimal.valueOf(bet.getEventOdds().getOdd()))
                        .setScale(2, RoundingMode.HALF_UP);
                User user = bet.getUser();
                user.setBalance(user.getBalance().add(payout));
                userRepository.save(user);
                totalPaid = totalPaid.add(payout);
            } else {
                bet.setStatus(BetStatus.LOST.name());
            }
            betRepository.save(bet);
        }

        log.info("Settled {} bets for event {}. Total paid {}", settled, eventId, totalPaid);
        return EventOutcomeResponseDto.builder()
                .eventId(eventId)
                .winnerDriverId(winnerDriverId)
                .betsSettled(settled)
                .totalPaid(totalPaid)
                .build();
    }
}
