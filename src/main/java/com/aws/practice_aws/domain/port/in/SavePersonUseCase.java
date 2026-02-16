package com.aws.practice_aws.domain.port.in;

import com.aws.practice_aws.domain.model.Person;

public interface SavePersonUseCase {

    Person save(Person person);
}
