package com.aws.practice_aws.domain.model;

import java.util.Objects;

public record Person(String identificationNumber, String name, String email) {

    public Person(String identificationNumber, String name, String email) {
        this.identificationNumber = Objects.requireNonNull(identificationNumber, "identificationNumber is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.email = Objects.requireNonNull(email, "email is required");
    }

}
