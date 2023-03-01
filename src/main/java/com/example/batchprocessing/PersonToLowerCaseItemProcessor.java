package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PersonToLowerCaseItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonToLowerCaseItemProcessor.class);

    @Override
    public Person process(Person person) {
        final String firstName = person.getFirstName().toLowerCase();
        final String lastName = person.getLastName().toLowerCase();
        final Person transformedPerson = new Person(firstName, lastName);
        log.info("Converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }
}
