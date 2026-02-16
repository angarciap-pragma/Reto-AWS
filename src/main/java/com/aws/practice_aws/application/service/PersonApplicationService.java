package com.aws.practice_aws.application.service;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.domain.port.in.FindPersonUseCase;
import com.aws.practice_aws.domain.port.in.SavePersonUseCase;
import com.aws.practice_aws.domain.port.out.PersonRepository;
import com.aws.practice_aws.infrastructure.config.properties.PersonMessagesProperties;
import com.aws.practice_aws.shared.exception.ResourceAlreadyExistsException;
import com.aws.practice_aws.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonApplicationService implements SavePersonUseCase, FindPersonUseCase {

    private final PersonRepository personRepository;
    private final PersonMessagesProperties personMessagesProperties;

    @Override
    public Person save(Person person) {
        if (personRepository.existsByIdentificationNumber(person.identificationNumber())) {
            throw new ResourceAlreadyExistsException(personMessagesProperties.alreadyExists());
        }
        return personRepository.save(person);
    }

    @Override
    public Person findByIdentificationNumber(String identificationNumber) {
        return personRepository.findByIdentificationNumber(identificationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(personMessagesProperties.notFound()));
    }
}
