package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.UserBetDto;
import com.sportygroup.f1betting.controller.dto.response.UserDetailsDto;
import com.sportygroup.f1betting.controller.dto.response.UserDto;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exception.UserNotFoundException;
import com.sportygroup.f1betting.repository.BetRepository;
import com.sportygroup.f1betting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BetRepository betRepository;

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDetailsDto getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        List<UserBetDto> bets = betRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserBetDto::new)
                .toList();
        return new UserDetailsDto(user, bets);
    }
}
