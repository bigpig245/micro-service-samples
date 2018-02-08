package com.example.config;

import com.example.dto.SignInDto;
import com.example.utils.Constants;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class CustomerAuthenticationFilter implements Filter {

    private static final String AUTHENTICATION_PATH = "/v1/authenticate";
    private static final Splitter AUTHORIZATION_SPLITTER =
            Splitter.on(CharMatcher.whitespace())
                    .omitEmptyStrings()
                    .trimResults()
                    .limit(2);
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final int AUTH_ELEMENTS_COUNT = 2;
    private static final int TOKEN_TYPE_INDEX = 0;
    private static final int TOKEN_INDEX = 1;

    public static final String COOKIE_NAME = "auth_cookie";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) servletRequest;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Map<String, String> headerValue = new HashMap<>();

        if (AUTHENTICATION_PATH.equals(httpReq.getServletPath()) && HttpMethod.POST.matches(httpReq.getMethod())) {
            SignInDto signInDto = Constants.MAPPER.readValue(httpReq.getInputStream(), SignInDto.class);
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(signInDto.getLogin(), signInDto.getPassword());
            headerValue.put("user-agent", httpReq.getHeader("user-agent"));
            usernamePasswordAuthenticationToken.setDetails(headerValue);
            securityContext.setAuthentication(usernamePasswordAuthenticationToken);
        } else if (securityContext.getAuthentication() == null) {
            String accessToken = findAccessToken(httpReq);
            if (accessToken != null) {
                securityContext.setAuthentication(
                        new PreAuthenticatedAuthenticationToken(accessToken, null));
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }

    private static String findAccessToken(HttpServletRequest httpReq) {
        String authHeader = httpReq.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<Cookie> cookieAuth = Stream.of(Optional.ofNullable(httpReq.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst();

        if (cookieAuth.isPresent()) {
            return cookieAuth.get().getValue();
        } else if (authHeader != null) {
            List<String> authElements = Lists.newArrayList(AUTHORIZATION_SPLITTER.split(authHeader));

            if (authElements.size() == AUTH_ELEMENTS_COUNT &&
                    BEARER_TOKEN_TYPE.equalsIgnoreCase(authElements.get(TOKEN_TYPE_INDEX))) {
                return authElements.get(TOKEN_INDEX);
            }
        }

        return null;
    }

    @Override
    public void destroy() {

    }

}
