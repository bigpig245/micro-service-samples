package com.example.config;

import com.example.dto.UserDto;
import com.example.dto.enumeration.BUMessage;
import com.example.exception.CustomRuntimeException;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        UserDto customer = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            customer = authenticateViaSignIn(authentication);
        } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            customer = authenticateViaAccessToken(authentication);
        }

        return customer == null ? null :
                new UsernamePasswordAuthenticationToken(customer, null, Collections.emptyList());
    }

    private UserDto authenticateViaSignIn(Authentication authentication) {
        try {
            return userService.signIn(authentication.getPrincipal().toString(),
                    (char[]) authentication.getCredentials(),
                    Optional.ofNullable(((Map) authentication.getDetails()).get("user-agent"))
                            .orElse("").toString());
        } catch (CustomRuntimeException e) {
            if (BUMessage.DEACTIVATED_CUSTOMER.getCode().equals(e.getBuMessage().getCode())) {
                throw new DisabledException("Deactivated", e);
            } else if (BUMessage.REFRESH_REGISTRATION_TOKEN.getCode().equals(e.getBuMessage().getCode())) {
                throw new AccountExpiredException("Activation mail expired", e);
            }
            throw new BadCredentialsException("Bad credentials", e);
        }
    }

    private UserDto authenticateViaAccessToken(Authentication authentication) {
        try {
            return userService.signInByAccessToken(authentication.getPrincipal().toString());
        } catch (CustomRuntimeException e) {
            throw new CredentialsExpiredException("Invalid access token", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

}
