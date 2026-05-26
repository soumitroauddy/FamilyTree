package com.familytree.usermgmt.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maps Railway {@code DATABASE_URL} to {@code spring.datasource.*} when
 * {@code SPRING_DATASOURCE_URL} is not explicitly set.
 */
public class RailwayEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String RAILWAY_ENV = "RAILWAY_ENVIRONMENT_NAME";
    private static final String PROPERTY_SOURCE = "railwayConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getProperty(RAILWAY_ENV) == null) {
            return;
        }

        if (StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
            return;
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        Optional<DatabaseUrlParser.ParsedDatasource> parsed = DatabaseUrlParser.parse(databaseUrl);
        if (parsed.isEmpty()) {
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        DatabaseUrlParser.ParsedDatasource ds = parsed.get();
        properties.put("spring.datasource.url", ds.jdbcUrl());
        properties.put("spring.datasource.username", ds.username());
        properties.put("spring.datasource.password", ds.password());

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
    }
}
