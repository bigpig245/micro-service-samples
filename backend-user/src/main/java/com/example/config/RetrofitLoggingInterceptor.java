package com.example.config;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class RetrofitLoggingInterceptor implements Interceptor {

    private final HttpLoggingInterceptor.Level level;
    private final Consumer<String> logConsumer;

    public RetrofitLoggingInterceptor(HttpLoggingInterceptor.Level level, Consumer<String> logConsumer) {
        this.level = level;
        this.logConsumer = requireNonNull(logConsumer, "A log consumer must be defined");
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RetrofitLoggingInterceptor.LoggerAggregator loggerAggregator = new RetrofitLoggingInterceptor.LoggerAggregator();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(loggerAggregator);
        ofNullable(level).ifPresent(interceptor::setLevel);
        try {
            return interceptor.intercept(chain);
        } finally {
            ofNullable(loggerAggregator.getAndFlush()).ifPresent(logConsumer);
        }
    }

    private static class LoggerAggregator implements HttpLoggingInterceptor.Logger {

        private StringBuilder logAggregator = new StringBuilder();

        @Override
        public void log(String message) {
            logAggregator.append(message).append("\n");
        }

        String getAndFlush() {
            String logMessage = logAggregator.toString();
            logAggregator = new StringBuilder();
            if (logMessage.isEmpty()) {
                return null;
            }
            return logMessage;
        }
    }
}