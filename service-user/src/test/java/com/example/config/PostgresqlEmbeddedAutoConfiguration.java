package com.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean;

import javax.sql.DataSource;

@Configuration
@Profile(value = "test")
public class PostgresqlEmbeddedAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() throws ClassNotFoundException {
        EmbeddedDatabaseFactoryBean embeddedDatabase = new EmbeddedDatabaseFactoryBean();
        embeddedDatabase.setDatabaseConfigurer(PostgresqlEmbeddedDatabaseConfigurer.getInstance());
        return embeddedDatabase.getDatabase();
    }

}
