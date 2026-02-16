package com.aws.practice_aws.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api")
public record ApiProperties(Paths paths, Errors errors) {

    public record Paths(String basePath, String savePerson, String findPerson) {
    }

    public record Errors(String unexpectedCode,
                         String unexpectedMessage,
                         String validationCode,
                         String validationMessage,
                         String notFoundCode,
                         String notFoundMessage,
                         String methodNotAllowedCode,
                         String methodNotAllowedMessage,
                         String unsupportedMediaTypeCode,
                         String unsupportedMediaTypeMessage,
                         String notAcceptableCode,
                         String notAcceptableMessage) {
    }
}
