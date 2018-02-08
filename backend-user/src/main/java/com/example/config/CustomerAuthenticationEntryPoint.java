package com.example.config;

import com.example.dto.ErrorDto;
import com.example.dto.enumeration.BUMessage;
import com.example.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CustomerAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
        log.debug("Authentication failed", e);
        BUMessage message = BUMessage.AUTHENTICATION_FAILED;
        if (e instanceof CredentialsExpiredException) {
            message = BUMessage.TOKEN_EXPIRED;
        } else if (e instanceof DisabledException) {
            message = BUMessage.DEACTIVATED_CUSTOMER;
        } else if (e instanceof AccountExpiredException) {
            message = BUMessage.REFRESH_REGISTRATION_TOKEN;
        }
        ErrorDto dto = ErrorDto.builder().code(message.getCode()).message(message.getMessage()).build();
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        Constants.MAPPER.writeValue(httpServletResponse.getOutputStream(), dto);
    }
}
