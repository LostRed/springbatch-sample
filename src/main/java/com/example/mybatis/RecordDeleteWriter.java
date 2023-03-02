package com.example.mybatis;

import com.example.batchprocessing.Record;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordDeleteWriter implements ItemWriter<List<Record>> {
    @Autowired
    private MyBatisBatchItemWriter<Record> recordDeleteItemWriter;

    @Override
    public void write(List<? extends List<Record>> items) {
        for (List<Record> item : items) {
            recordDeleteItemWriter.write(item);
        }
    }
}
