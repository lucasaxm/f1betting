package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.BetResponseDto;
import com.sportygroup.f1betting.entity.*;
import com.sportygroup.f1betting.exception.*;
import com.sportygroup.f1betting.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    EventOddRepository eventOddRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    CurrentEventOddsViewRepository oddsViewRepository;
    @Mock
    BetRepository betRepository;

    @InjectMocks
    BetService betService;

    User user;
    Event event;
    EventOdd odd;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("U");
        user.setBalance(new BigDecimal("100.00"));

        event = new Event();
        event.setId(UUID.randomUUID());

        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());

        odd = new EventOdd();
        odd.setId(UUID.randomUUID());
        odd.setEvent(event);
        odd.setDriver(driver);
        odd.setOdd((short)3);
        odd.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void placeBetHappyPath() {
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(eventOddRepository.findById(odd.getId())).thenReturn(Optional.of(odd));
        when(oddsViewRepository.existsById(odd.getId())).thenReturn(true);
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));
        when(betRepository.save(any())).thenAnswer(invocation -> {
            Bet b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BetResponseDto dto = betService.placeBet(user.getId(), odd.getId(), new BigDecimal("25.50"));

        assertThat(dto.remainingBalance()).isEqualByComparingTo("74.50");
        verify(betRepository).save(any());
        verify(userRepository).save(user);
    }

    @Test
    void insufficientBalanceThrows() {
        user.setBalance(new BigDecimal("10"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(InsufficientBalanceException.class,
            () -> betService.placeBet(user.getId(), odd.getId(), new BigDecimal("25")));
    }

    @Test
    void eventClosedThrows() {
        event.setWinnerDriverId(UUID.randomUUID());
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(eventOddRepository.findById(odd.getId())).thenReturn(Optional.of(odd));
        when(oddsViewRepository.existsById(odd.getId())).thenReturn(true);
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));

        assertThrows(EventClosedException.class,
            () -> betService.placeBet(user.getId(), odd.getId(), new BigDecimal("10")));
    }

    @Test
    void outdatedOddThrows() {
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(eventOddRepository.findById(odd.getId())).thenReturn(Optional.of(odd));
        when(oddsViewRepository.existsById(odd.getId())).thenReturn(false);

        assertThrows(OutdatedEventOddException.class,
            () -> betService.placeBet(user.getId(), odd.getId(), new BigDecimal("10")));
    }

    @Test
    void unknownOddThrows() {
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(eventOddRepository.findById(odd.getId())).thenReturn(Optional.empty());

        assertThrows(EventOddNotFoundException.class,
            () -> betService.placeBet(user.getId(), odd.getId(), new BigDecimal("10")));
    }
}
