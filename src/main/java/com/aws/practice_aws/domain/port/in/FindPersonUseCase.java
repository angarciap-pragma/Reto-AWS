package com.aws.practice_aws.domain.port.in;

import com.aws.practice_aws.domain.model.Person;

public interface FindPersonUseCase {

    Person findByIdentificationNumber(String identificationNumber);
}
