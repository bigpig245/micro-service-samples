package com.example.dto.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.PackagePrivate;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SUMessage {
    @PackagePrivate
    OK("0", "SUCCESS", HttpStatus.OK),
    @PackagePrivate
    INACTIVE_USER("ERR_SU_1000", "User is not activated", HttpStatus.BAD_REQUEST);

    String code;

    String message;

    HttpStatus httpStatus;
}
