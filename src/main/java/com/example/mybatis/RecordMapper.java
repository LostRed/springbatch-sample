package com.example.mybatis;

import com.example.batchprocessing.Record;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecordMapper {
    int delete(Record record);

    int insertBatch(List<Record> records);
}
