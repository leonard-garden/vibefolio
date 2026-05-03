package com.vibefolio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 metadata + security scheme declaration.
 * The spec is exported via: {@code ./mvnw springdoc-openapi:generate}
 */
@Configuration
class OpenApiConfig {

    private static final String API_KEY_SCHEME = "X-API-Key";

    @Bean
    OpenAPI vibefolioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vibefolio API")
                        .version("1.0.0")
                        .description("AI-powered CV → portfolio generator. All endpoints require X-API-Key header."))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME,
                                new SecurityScheme()
                                        .name(API_KEY_SCHEME)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)));
    }
}
