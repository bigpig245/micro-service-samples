package com.example.dto.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.PackagePrivate;
import org.springframework.http.HttpStatus;

import java.util.Collection;

import static com.example.utils.Constants.SEPARATOR;

@Getter
@AllArgsConstructor
public enum BUMessage {
    @PackagePrivate
    OK("0", "SUCCESS", HttpStatus.OK),
    @PackagePrivate
    MISSING_COUNTRY_WORKFLOW("ERR_BU_1000", "Unknown Country", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    MISSING_REQUEST_PARAMETER("ERR_BU_1001", "Missing request param", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    INVALID_REQUEST_PARAMETER("ERR_BU_1002", "Invalid request param", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    UPLOAD_MEDIA_FILE_SIZE_ERROR("ERR_BU_1003", "Invalid file size", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    UPLOAD_MEDIA_NO_FILE_SELECTED_ERROR("ERR_BU_1004", "Unsupported file", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    UNKNOWN_ERROR("ERR_BU_1005", "Unknown error", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    DEACTIVATED_CUSTOMER("ERR_BU_1006", "Deactivated User", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    REFRESH_REGISTRATION_TOKEN("ERR_BU_1007", "Token is expired", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    TOKEN_EXPIRED("ERR_BU_1008", "Token is expired", HttpStatus.BAD_REQUEST),
    @PackagePrivate
    AUTHENTICATION_FAILED("ERR_BU_1009", "Authentication failed", HttpStatus.BAD_REQUEST);

    String code;

    String message;

    HttpStatus httpStatus;

    public String getMessage(Collection<String> errorValues) {
        return String.format(message, String.join(SEPARATOR, errorValues));
    }
}
