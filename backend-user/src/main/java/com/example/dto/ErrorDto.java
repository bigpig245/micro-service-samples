package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDto {

    /**
     * Code Id
     */
    String code;

    /**
     * Message detail
     */
    String message;

    /**
     * Stacktrace
     */
    String stackTrace;

    List<ErrorFieldDto> errors;

}
