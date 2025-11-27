package com.courserec.rating.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI ratingServiceOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Rating Service API")
                .description("Rating submission and event publishing service API")
                .version("1.0.0")
                .contact(new Contact().name("Course Recommendation Platform").email("support@courserec.com"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")))
        .servers(
            List.of(
                new Server().url("http://localhost:8083").description("Local development server"),
                new Server().url("http://rating-service:8083").description("Docker service")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token (get it from UserService /api/v1/auth/login)")));
  }
}

