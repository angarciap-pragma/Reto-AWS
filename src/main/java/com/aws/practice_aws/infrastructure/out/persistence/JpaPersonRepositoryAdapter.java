package com.aws.practice_aws.infrastructure.out.persistence;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.domain.port.out.PersonRepository;
import com.aws.practice_aws.infrastructure.out.persistence.entity.PersonEntity;
import com.aws.practice_aws.infrastructure.out.persistence.repository.SpringDataPersonRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPersonRepositoryAdapter implements PersonRepository {

    private final SpringDataPersonRepository springDataPersonRepository;

    public JpaPersonRepositoryAdapter(SpringDataPersonRepository springDataPersonRepository) {
        this.springDataPersonRepository = springDataPersonRepository;
    }

    @Override
    public Person save(Person person) {
        PersonEntity savedEntity = springDataPersonRepository.save(PersonEntity.fromDomain(person));
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Person> findByIdentificationNumber(String identificationNumber) {
        return springDataPersonRepository.findById(identificationNumber).map(PersonEntity::toDomain);
    }

    @Override
    public boolean existsByIdentificationNumber(String identificationNumber) {
        return springDataPersonRepository.existsById(identificationNumber);
    }
}
