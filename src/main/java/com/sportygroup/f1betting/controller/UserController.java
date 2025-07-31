package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.controller.dto.response.UserDetailsDto;
import com.sportygroup.f1betting.controller.dto.response.UserDto;
import com.sportygroup.f1betting.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/{userId}")
    public UserDetailsDto getUser(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }
}
