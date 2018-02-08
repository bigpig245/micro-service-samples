package com.example.config;

import com.example.dto.enumeration.Country;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class CountryInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Country country = CountryFilter.CURRENT_COUNTRY.get();

        if (!StringUtils.isEmpty(country)) {
            request = request.newBuilder()
                    .addHeader(CountryFilter.HTTP_HEADER_KEY, country.name())
                    .build();
        }

        return chain.proceed(request);
    }
}
