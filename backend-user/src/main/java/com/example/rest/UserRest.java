package com.example.rest;

import com.example.dto.CreatedUserDto;
import com.example.dto.SignInDto;
import com.example.dto.UserDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

import javax.validation.Valid;

public interface UserRest {
    @POST("v1/users")
    Call<Void> addUser(@Body @Valid CreatedUserDto createdUserDto);
    @POST("v1/signIn")
    Call<UserDto> signIn(@Body @Valid SignInDto signInDto);
    @POST("v1/users/activate")
    Call<Void> activate(@Query("activateToken") String activateToken);
}
