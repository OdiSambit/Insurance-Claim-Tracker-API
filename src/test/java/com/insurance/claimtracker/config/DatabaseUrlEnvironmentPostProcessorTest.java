package com.insurance.claimtracker.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor postProcessor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    @DisplayName("converts a Neon-style postgresql:// URL into JDBC settings")
    void convertsPostgresUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL",
                        "postgresql://neondb_owner:s3cr3t@ep-cool-1.us-east-1.aws.neon.tech:5432/neondb?sslmode=require");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://ep-cool-1.us-east-1.aws.neon.tech:5432/neondb?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("neondb_owner");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("s3cr3t");
        assertThat(environment.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
    }

    @Test
    @DisplayName("supports the short postgres:// scheme and URL-encoded credentials")
    void convertsShortSchemeWithEncodedCredentials() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://user:p%40ss@db.example.com:5432/app");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com:5432/app");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("p@ss");
    }

    @Test
    @DisplayName("is a no-op when DATABASE_URL is absent")
    void noOpWithoutDatabaseUrl() {
        MockEnvironment environment = new MockEnvironment();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
    }

    @Test
    @DisplayName("leaves an existing JDBC URL untouched")
    void ignoresJdbcUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "jdbc:postgresql://localhost:5432/insurance");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
    }
}
