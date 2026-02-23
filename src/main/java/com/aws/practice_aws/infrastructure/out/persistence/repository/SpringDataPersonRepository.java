package com.aws.practice_aws.infrastructure.out.persistence.repository;

import com.aws.practice_aws.infrastructure.out.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPersonRepository extends JpaRepository<PersonEntity, String> {
}
