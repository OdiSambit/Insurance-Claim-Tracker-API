package com.insurance.claimtracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI insuranceClaimTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Insurance Claim Tracker API")
                        .description("REST API to create property and casualty insurance policies, "
                                + "calculate premiums, file claims and move them through a strict review lifecycle.")
                        .version("1.0.0")
                        .contact(new Contact().name("Insurance Claim Tracker"))
                        .license(new License().name("MIT")));
    }
}
