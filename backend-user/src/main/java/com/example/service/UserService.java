package com.example.service;

import com.example.dto.CreatedUserDto;
import com.example.dto.SignInDto;
import com.example.dto.UserDto;
import com.example.rest.UserRest;
import com.example.utils.RestClientHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRest userRest;

    public void addUser(CreatedUserDto createdUserDto) {
        RestClientHelper.execute(userRest.addUser(createdUserDto));
    }

    public UserDto signIn(String login, char[] password, String userAgent) {
        return RestClientHelper.execute(userRest.signIn(
                SignInDto.builder().login(login).password(password).build())).body();
    }


    public UserDto signInByAccessToken(String accessToken) {
        return RestClientHelper.execute(userRest.signIn(
                SignInDto.builder().login(accessToken).build())).body();
    }


    public void activate(String activateToken) {
        RestClientHelper.execute(userRest.activate(activateToken));
    }
}
