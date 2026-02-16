package com.aws.practice_aws.application.service;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.domain.port.out.PersonRepository;
import com.aws.practice_aws.infrastructure.config.properties.PersonMessagesProperties;
import com.aws.practice_aws.shared.exception.ResourceAlreadyExistsException;
import com.aws.practice_aws.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonApplicationServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMessagesProperties personMessagesProperties;

    @InjectMocks
    private PersonApplicationService personApplicationService;

    @Test
    void shouldSavePersonWhenPersonDoesNotExist() {
        Person person = new Person("1001", "Andrea Garcia", "andrea@demo.com");
        when(personRepository.existsByIdentificationNumber("1001")).thenReturn(false);
        when(personRepository.save(person)).thenReturn(person);

        Person saved = personApplicationService.save(person);

        assertEquals("1001", saved.identificationNumber());
    }

    @Test
    void shouldThrowConflictWhenPersonAlreadyExists() {
        Person person = new Person("1001", "Andrea Garcia", "andrea@demo.com");
        when(personRepository.existsByIdentificationNumber("1001")).thenReturn(true);
        when(personMessagesProperties.alreadyExists()).thenReturn("La persona ya existe");

        assertThrows(ResourceAlreadyExistsException.class, () -> personApplicationService.save(person));
    }

    @Test
    void shouldThrowNotFoundWhenPersonDoesNotExist() {
        when(personRepository.findByIdentificationNumber("9999")).thenReturn(Optional.empty());
        when(personMessagesProperties.notFound()).thenReturn("La persona no existe");

        assertThrows(ResourceNotFoundException.class, () -> personApplicationService.findByIdentificationNumber("9999"));
    }
}
