package com.example.controller;

import com.example.dto.CreatedUserDto;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreatedUserDto createdUserDto) {
        userService.addUser(createdUserDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<Void> create(@RequestParam String activationToken) {
        userService.activate(activationToken);
        return ResponseEntity.noContent().build();
    }

}
