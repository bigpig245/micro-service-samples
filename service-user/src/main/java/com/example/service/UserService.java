package com.example.service;

import com.example.domain.User;
import com.example.dto.CreatedUserDto;
import com.example.dto.UserDto;
import com.example.dto.UserLoginDto;
import com.example.dto.enumeration.SUMessage;
import com.example.exception.CustomRuntimeException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRED_DAYS = 2;

    public UserDto getUserInfo(UserLoginDto loginDto) {
        return ofNullable(userRepository.findByLogin(loginDto.getLogin()))
                .filter(User::isActive)
                .map(userMapper::userToUserDto)
                .orElseThrow(() -> new CustomRuntimeException(SUMessage.INACTIVE_USER));
    }

    @Transactional
    public void addUser(CreatedUserDto createdUserDto) {
        User user = userMapper.createUserDtoToUser(createdUserDto, passwordEncoder);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setActivationExpiredDate(LocalDateTime.now().plusDays(EXPIRED_DAYS));
        userRepository.save(user);
    }
}
