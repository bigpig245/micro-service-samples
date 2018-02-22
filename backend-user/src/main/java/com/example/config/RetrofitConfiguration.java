package com.example.config;

import com.example.rest.UserRest;
import com.example.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RetrofitConfiguration {

    private final ServiceUrlConfig serviceUrlConfig;
    private final ServiceTimeOutConfig serviceTimeOutConfig;
    private final Interceptor countryInterceptor = new CountryInterceptor();
    private final Interceptor loggingInterceptor
            = new RetrofitLoggingInterceptor(HttpLoggingInterceptor.Level.BODY, log::trace);


    @Bean
    public UserRest userRest() {
        return createClient(serviceUrlConfig.getUser(), UserRest.class, this::externalHttpClient);
    }


    @Bean
    public OkHttpClient externalHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(countryInterceptor)
                .readTimeout(serviceTimeOutConfig.getInternalServices(), TimeUnit.SECONDS)
                .build();
    }

    private <T> T createClient(String baseUrl, Class<T> clazz, Supplier<OkHttpClient> clientSupplier) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientSupplier.get())
                .addConverterFactory(JacksonConverterFactory.create(Constants.MAPPER))
                .build()
                .create(clazz);
    }


}