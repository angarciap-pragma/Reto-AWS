package com.aws.practice_aws.infrastructure.in.rest.mapper;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.infrastructure.in.rest.dto.PersonResponse;
import com.aws.practice_aws.infrastructure.in.rest.dto.SavePersonRequest;
import org.springframework.stereotype.Component;

@Component
public class PersonRestMapper {

    public Person toDomain(SavePersonRequest request) {
        return new Person(request.identificationNumber(), request.name(), request.email());
    }

    public PersonResponse toResponse(Person person) {
        return new PersonResponse(person.identificationNumber(), person.name(), person.email());
    }
}
