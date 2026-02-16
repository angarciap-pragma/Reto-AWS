package com.aws.practice_aws.infrastructure.in.rest.controller;

import com.aws.practice_aws.domain.model.Person;
import com.aws.practice_aws.domain.port.in.FindPersonUseCase;
import com.aws.practice_aws.domain.port.in.SavePersonUseCase;
import com.aws.practice_aws.infrastructure.config.properties.ApiProperties;
import com.aws.practice_aws.infrastructure.in.rest.dto.PersonResponse;
import com.aws.practice_aws.infrastructure.in.rest.dto.SavePersonRequest;
import com.aws.practice_aws.infrastructure.in.rest.mapper.PersonRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${app.api.paths.base-path}")
@RequiredArgsConstructor
@Validated
public class PersonController {

    private final SavePersonUseCase savePersonUseCase;
    private final FindPersonUseCase findPersonUseCase;
    private final ApiProperties apiProperties;
    private final PersonRestMapper personRestMapper;

    @PostMapping(
            value = "${app.api.paths.save-person}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonResponse> savePerson(@Valid @RequestBody SavePersonRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        Person person = personRestMapper.toDomain(request);
        Person savedPerson = savePersonUseCase.save(person);
        URI location = uriBuilder.path(apiProperties.paths().basePath() + apiProperties.paths().findPerson())
                .buildAndExpand(savedPerson.identificationNumber())
                .toUri();

        log.info("person.save.success identificationNumber={}", savedPerson.identificationNumber());
        return ResponseEntity.created(location).body(personRestMapper.toResponse(savedPerson));
    }

    @GetMapping(value = "/consultarpersona/{identificationNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonResponse> findPerson(
            @PathVariable("identificationNumber")
            @NotBlank(message = "{person.identification.required}")
            @Size(max = 30, message = "{person.identification.size}")
            String identificationNumber) {
        Person foundPerson = findPersonUseCase.findByIdentificationNumber(identificationNumber);
        log.info("person.find.success identificationNumber={}", identificationNumber);
        return ResponseEntity.ok(personRestMapper.toResponse(foundPerson));
    }
}
