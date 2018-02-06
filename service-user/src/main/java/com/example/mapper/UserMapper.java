package com.example.mapper;

import com.example.domain.User;
import com.example.dto.CreatedUserDto;
import com.example.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.CharBuffer;

@Mapper(componentModel = "spring",
        imports = {CharBuffer.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto userToUserDto(User user);

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(CharBuffer.wrap(createdUserDto.getPassword())))")
    User createUserDtoToUser(CreatedUserDto createdUserDto, PasswordEncoder passwordEncoder);
}
