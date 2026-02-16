package com.aws.practice_aws.infrastructure.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SavePersonRequest(
        @NotBlank(message = "{person.identification.required}")
        @Size(max = 30, message = "{person.identification.size}")
        String identificationNumber,
        @NotBlank(message = "{person.name.required}")
        @Size(max = 120, message = "{person.name.size}")
        String name,
        @NotBlank(message = "{person.email.required}")
        @Email(message = "{person.email.format}")
        String email
) {
}
