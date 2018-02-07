package com.example.dbunit;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbUnitConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public DatabaseConfigBean databaseConfigBean() {
        DatabaseConfigBean databaseConfigBean = new DatabaseConfigBean();
        databaseConfigBean.setSkipOracleRecyclebinTables(true);
        databaseConfigBean.setDatatypeFactory(new PostgresqlDataTypeFactory());
        return databaseConfigBean;
    }

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection() {
        DatabaseDataSourceConnectionFactoryBean factory = new DatabaseDataSourceConnectionFactoryBean(dataSource);
        factory.setSchema("service_user");
        factory.setDatabaseConfig(databaseConfigBean());
        return factory;
    }
}
