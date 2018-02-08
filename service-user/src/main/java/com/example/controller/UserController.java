package com.example.controller;

import com.example.dto.CreatedUserDto;
import com.example.dto.UserDto;
import com.example.dto.UserLoginDto;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping(value = "/v1/signIn")
    public ResponseEntity<UserDto> signInEmail(@RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.getUserInfo(dto));
    }

    @PostMapping(value = "/v1/users")
    public ResponseEntity<Void> create(@RequestBody CreatedUserDto createdUserDto) {
        userService.addUser(createdUserDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/v1/users/activate")
    public ResponseEntity<Void> activate(@RequestParam String activateToken) {
        userService.activate(activateToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/v1/users/{login}")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        return ResponseEntity.ok(userService.getUserInfo(UserLoginDto.builder().login(login).build()));
    }
}
