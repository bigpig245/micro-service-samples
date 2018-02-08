package com.example.config;

import com.example.dto.enumeration.BUMessage;
import com.example.dto.enumeration.Country;
import com.example.exception.CustomRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CountryFilter implements Filter {

    public static final String HTTP_HEADER_KEY = "x-country-workflow";

    public static final ThreadLocal<Country> CURRENT_COUNTRY = new ThreadLocal<>();

    private final CountryURLProperties countryURLProperties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        String serverName = servletRequest.getServerName();
        if (serverName.equals(countryURLProperties.getVn())) {
            CURRENT_COUNTRY.set(Country.VN);
        } else if (serverName.equals(countryURLProperties.getEn())) {
            CURRENT_COUNTRY.set(Country.EN);
        } else {
            log.error("error access to API with serverName:{} and configUrlVn:{} and configUrlEn:{}",
                    serverName, countryURLProperties.getVn(), countryURLProperties.getEn());
            throw new CustomRuntimeException(BUMessage.MISSING_COUNTRY_WORKFLOW);
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            CURRENT_COUNTRY.remove();
        }
    }

    @Override
    public void destroy() {

    }
}

