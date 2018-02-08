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
    INACTIVE_USER("ERR_SU_1000", "User is not activated", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    RESOURCE_NOT_FOUND("ERR_SU_1001", "User is found", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    EXPIRED_TOKEN("ERR_SU_1002", "Token is expired", HttpStatus.BAD_REQUEST);

    String code;

    String message;

    HttpStatus httpStatus;
}
