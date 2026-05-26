package com.familytree.usermgmt.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class RailwayEnvironmentPostProcessorTest {

    private final RailwayEnvironmentPostProcessor processor = new RailwayEnvironmentPostProcessor();

    @Test
    void noOpOutsideRailway() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://u:p@remote:5432/db");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url")).isNull();
    }

    @Test
    void mapsDatabaseUrlOnRailway() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("RAILWAY_ENVIRONMENT_NAME", "production");
        env.setProperty(
                "DATABASE_URL",
                "postgresql://familytree:secret@containers-us-west-123.railway.app:5432/railway");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo(
                        "jdbc:postgresql://containers-us-west-123.railway.app:5432/railway?sslmode=require");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("familytree");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secret");
    }

    @Test
    void mapsDatabaseUrlEvenWhenApplicationYamlDefinesDefaultDatasource() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("RAILWAY_ENVIRONMENT_NAME", "production");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://db.example.com:5432/postgres");
        env.setProperty(
                "DATABASE_URL",
                "postgresql://familytree:secret@containers-us-west-123.railway.app:5432/railway");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo(
                        "jdbc:postgresql://containers-us-west-123.railway.app:5432/railway?sslmode=require");
    }

    @Test
    void respectsExplicitSpringDatasourceUrl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("RAILWAY_ENVIRONMENT_NAME", "production");
        env.setProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://custom:5432/db");
        env.setProperty(
                "DATABASE_URL",
                "postgresql://familytree:secret@containers-us-west-123.railway.app:5432/railway");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url")).isNull();
    }
}
