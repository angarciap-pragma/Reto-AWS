package com.aws.practice_aws.infrastructure.out.persistence;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.domain.port.out.PersonRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPersonRepository implements PersonRepository {

    private final Map<String, Person> people = new ConcurrentHashMap<>();

    @Override
    public Person save(Person person) {
        people.put(person.identificationNumber(), person);
        return person;
    }

    @Override
    public Optional<Person> findByIdentificationNumber(String identificationNumber) {
        return Optional.ofNullable(people.get(identificationNumber));
    }

    @Override
    public boolean existsByIdentificationNumber(String identificationNumber) {
        return people.containsKey(identificationNumber);
    }
}
