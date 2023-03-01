package com.example.batchprocessing;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersonItemProcessor implements ItemProcessor<Person, List<Record>> {
    @Override
    public List<Record> process(Person item) {
        List<Record> list = new ArrayList<>();
        if (item.getFirstName().contains("J")) {
            Record record = new Record();
            record.setPersonId(item.getPersonId());
            record.setReason("姓中有字母J");
            list.add(record);
        }
        if (item.getFirstName().contains("L")) {
            Record record = new Record();
            record.setPersonId(item.getPersonId());
            record.setReason("姓中有字母L");
            list.add(record);
        }
        return list;
    }
}
