package com.example.mybatis;

import com.example.batchprocessing.Record;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecordWriter implements ItemWriter<List<Record>> {
    @Autowired
    private MyBatisBatchItemWriter<List<Record>> writer;
    private final SqlSessionTemplate sqlSessionTemplate;

    public RecordWriter(SqlSessionFactory sqlSessionFactory) {
        sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

    @Override
    public void write(List<? extends List<Record>> items) {
        List<Long> personIds = items.stream()
                .flatMap(Collection::stream)
                .map(Record::getPersonId)
                .distinct()
                .collect(Collectors.toList());
        sqlSessionTemplate.update("com.example.mybatis.RecordMapper.deleteBatch", personIds);
        writer.write(items);
    }
}
