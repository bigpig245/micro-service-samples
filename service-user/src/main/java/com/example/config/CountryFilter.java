package com.example.config;

import com.example.dto.enumeration.Country;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CountryFilter implements Filter {
    public static final String HTTP_HEADER_KEY = "x-country-workflow";

    public static final ThreadLocal<Country> CURRENT_COUNTRY = new ThreadLocal<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String header = ((HttpServletRequest) servletRequest).getHeader(HTTP_HEADER_KEY);
        if (!StringUtils.isEmpty(header)) {
            CURRENT_COUNTRY.set(Country.valueOf(header));
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
