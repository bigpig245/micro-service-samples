package com.example.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@RequiredArgsConstructor
@Configuration
public class HttpSessionConfig {

    private final ServerProperties serverProperties;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        if (serverProperties.getSession().getCookie().getSecure() != null) {
            cookieSerializer.setUseSecureCookie(serverProperties.getSession().getCookie().getSecure());
        }
        return cookieSerializer;
    }

    @Profile("!local")
    @Configuration
    @EnableRedisHttpSession(redisNamespace = "user")
    public static class RedisHttpSessionConfig {
        /*
         * http://docs.spring.io/spring-data/data-redis/docs/current/reference/html/
         */
        @Bean
        public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
            return new GenericJackson2JsonRedisSerializer(objectMapper());
        }

        /*
         * https://github.com/spring-projects/spring-session/tree/1.3.0.RELEASE/samples/httpsession-redis-json
         */
        private static ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            mapper.registerModule(new JavaTimeModule());
            mapper.registerModules(SecurityJackson2Modules.getModules(RedisHttpSessionConfig.class.getClassLoader()));
            // Override the Spring Security mixin with our own, to support a principal which is not a User
            mapper.addMixIn(UsernamePasswordAuthenticationToken.class, UsernamePasswordAuthenticationTokenMixin.class);
            return mapper;
        }

        @Bean
        public LettuceConnectionFactory connectionFactory() {
            RedisProperties props = redisProperties();
            return new LettuceConnectionFactory(props.getHost(), props.getPort());
        }

        @Bean
        @Primary
        public RedisProperties redisProperties() {
            return new RedisProperties();
        }

        @Bean
        public ConfigureRedisAction configureRedisAction() {
            return ConfigureRedisAction.NO_OP;
        }
    }
}
