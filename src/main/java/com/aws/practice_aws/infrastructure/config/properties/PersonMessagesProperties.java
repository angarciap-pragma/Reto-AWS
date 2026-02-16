package com.aws.practice_aws.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.person.messages")
public record PersonMessagesProperties(String alreadyExists, String notFound) {
}
