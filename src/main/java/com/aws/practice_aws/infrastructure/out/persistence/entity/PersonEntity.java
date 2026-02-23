package com.aws.practice_aws.infrastructure.out.persistence.entity;

import com.aws.practice_aws.domain.model.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persons")
public class PersonEntity {

    @Id
    @Column(name = "identification_number", nullable = false, length = 30)
    private String identificationNumber;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    protected PersonEntity() {
    }

    public PersonEntity(String identificationNumber, String name, String email) {
        this.identificationNumber = identificationNumber;
        this.name = name;
        this.email = email;
    }

    public static PersonEntity fromDomain(Person person) {
        return new PersonEntity(person.identificationNumber(), person.name(), person.email());
    }

    public Person toDomain() {
        return new Person(identificationNumber, name, email);
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }
}
