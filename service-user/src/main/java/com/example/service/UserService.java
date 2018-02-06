package com.example.service;

import com.example.domain.User;
import com.example.dto.CreatedUserDto;
import com.example.dto.UserDto;
import com.example.dto.UserLoginDto;
import lombok.RequiredArgsConstructor;
import com.example.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRED_DAYS = 2;

    public UserDto getUserInfo(UserLoginDto loginDto) {
        return userMapper.userToUserDto(userRepository.findByLogin(loginDto.getLogin()));
    }

    @Transactional
    public void addUser(CreatedUserDto createdUserDto) {
        User user = userMapper.createUserDtoToUser(createdUserDto, passwordEncoder);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setActivationExpiredDate(LocalDateTime.now().plusDays(EXPIRED_DAYS));
        userRepository.save(user);
    }
}
