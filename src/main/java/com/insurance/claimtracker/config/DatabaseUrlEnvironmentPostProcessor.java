package com.insurance.claimtracker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Allows hosting providers to supply a single {@code DATABASE_URL} (as given by Render, Railway,
 * Heroku and Neon) in the {@code postgres://user:pass@host:port/db?sslmode=require} form and have it
 * transparently converted into the JDBC settings Spring Boot expects.
 *
 * <p>When {@code DATABASE_URL} is absent the application keeps its default in-memory H2 datasource,
 * so local development and tests are unaffected.</p>
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlOverrides";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        String value = databaseUrl.trim();
        if (value.startsWith("jdbc:")) {
            return;
        }
        if (!value.startsWith("postgres://") && !value.startsWith("postgresql://")) {
            return;
        }

        URI uri = URI.create(value);
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("spring.datasource.url", toJdbcUrl(uri));
        overrides.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            overrides.put("spring.datasource.username", decode(parts[0]));
            if (parts.length > 1) {
                overrides.put("spring.datasource.password", decode(parts[1]));
            }
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
    }

    private String toJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getPath());
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getQuery());
        }
        return jdbcUrl.toString();
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
