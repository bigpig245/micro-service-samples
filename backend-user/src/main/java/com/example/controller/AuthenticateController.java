package com.example.controller;

import com.example.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthenticateController {
    @PostMapping(value = "/v1/authenticate")
    public ResponseEntity<UserDto> signIn(@AuthenticationPrincipal UserDto userDto) {
        return ResponseEntity.ok(UserDto.builder()
                .email(userDto.getEmail())
                .login(userDto.getLogin())
                .displayName(userDto.getDisplayName())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName()).build());
    }
}
