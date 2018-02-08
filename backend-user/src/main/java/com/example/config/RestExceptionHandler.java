package com.example.config;

import com.example.dto.ErrorDto;
import com.example.dto.ErrorFieldDto;
import com.example.dto.enumeration.BUMessage;
import com.example.exception.BackendCustomerException;
import com.example.exception.CustomRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Set<String> fieldNames = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .collect(Collectors.toCollection(TreeSet::new));
        List<ErrorFieldDto> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(ErrorFieldDto.builder().name(error.getField()).message(error.getDefaultMessage()).build());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            fieldErrors.add(
                    ErrorFieldDto.builder().name(error.getObjectName()).message(error.getDefaultMessage()).build());
        }
        return ErrorDto.builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .message(BUMessage.INVALID_REQUEST_PARAMETER.getMessage(fieldNames))
                .errors(fieldErrors)
                .build();
    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        Set<String> fieldNames = new HashSet<>();
        List<ErrorFieldDto> fieldErrors = new ArrayList<>();
        fieldNames.add(ex.getParameterName());
        fieldErrors.add(ErrorFieldDto.builder().name(ex.getParameterName()).message(ex.getMessage()).build());
        return ErrorDto.builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .message(BUMessage.MISSING_REQUEST_PARAMETER.getMessage(fieldNames))
                .errors(fieldErrors)
                .build();
    }

    @ExceptionHandler(value = {
            CustomRuntimeException.class,
            BackendCustomerException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleException(CustomRuntimeException ex) {
        return ErrorDto.builder()
                .code(ex.getBuMessage().getCode())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDto handleNotFoundResourceException(ResourceNotFoundException ex) {
        return ErrorDto.builder()
                .code(HttpStatus.NOT_FOUND.name())
                .message(ex.getMessage())
                .build();
    }

    // Catch file size exceeded exception
    @ExceptionHandler(value = {MultipartException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleMultipartException(MultipartException e) {
        log.error("Total files size is over 25MB OR one file in list has size over 10MB", e);
        return ErrorDto.builder()
                .code(BUMessage.UPLOAD_MEDIA_FILE_SIZE_ERROR.getCode())
                .message(BUMessage.UPLOAD_MEDIA_FILE_SIZE_ERROR.getMessage())
                .build();
    }

    // Catch file no selection
    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("File is empty: {}", e);
        return ErrorDto.builder()
                .code(BUMessage.UPLOAD_MEDIA_NO_FILE_SELECTED_ERROR.getCode())
                .message(BUMessage.UPLOAD_MEDIA_NO_FILE_SELECTED_ERROR.getMessage())
                .build();
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleConstraintViolation(ConstraintViolationException ex) {
        Map<Path, String> messages = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage));
        return ErrorDto.builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .message(BUMessage.INVALID_REQUEST_PARAMETER.getMessage(messages.keySet().stream()
                        .map(Path::toString).collect(Collectors.toList())))
                .errors(messages.entrySet().stream().map(pathStringEntry -> ErrorFieldDto
                        .builder()
                        .message(pathStringEntry.getValue())
                        .name(pathStringEntry.getKey().toString())
                        .build())
                        .collect(Collectors.toList()))
                .build();

    }

}
