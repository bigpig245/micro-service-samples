package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan("com.example.config")
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] PERMITTED_REQUESTS = {

            // Allow documentation infos
            "/swagger-ui.html",
            "/webjars/springfox-swagger-ui/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",

            // health checks
            "/health"
    };

    private final CustomerAuthenticationEntryPoint customerAuthenticationEntryPoint;
    private final CountryURLProperties countryURLProperties;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling()
                .authenticationEntryPoint(customerAuthenticationEntryPoint)
                .and()
                .addFilterBefore(new CustomerAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new CountryFilter(countryURLProperties), CustomerAuthenticationFilter.class)
                .antMatcher("/v*/**")
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/v*/users/activate",
                        "v*/users").permitAll()
                .antMatchers("/v*/users/activate").permitAll()
                .antMatchers(PERMITTED_REQUESTS).permitAll()
                .anyRequest().authenticated()
        ;
    }

}