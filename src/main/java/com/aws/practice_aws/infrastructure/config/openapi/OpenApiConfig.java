package com.aws.practice_aws.infrastructure.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI practiceAwsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Practice AWS Service API")
                .description("API para la gestion de personas")
                .version("v1")
                .contact(new Contact()
                        .name("Equipo Backend")
                        .email("backend@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
