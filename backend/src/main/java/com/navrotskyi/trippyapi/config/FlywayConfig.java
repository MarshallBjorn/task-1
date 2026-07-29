package com.navrotskyi.trippyapi.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .load();
        flyway.migrate();
        return flyway;
    }
}