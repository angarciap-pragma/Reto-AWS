package com.aws.practice_aws.domain.port.out;

import com.aws.practice_aws.domain.model.Person;

import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    Optional<Person> findByIdentificationNumber(String identificationNumber);

    boolean existsByIdentificationNumber(String identificationNumber);
}
