package com.example.mybatis;

import com.example.batchprocessing.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface RecordMapper {
    int deleteByPersonId(Long personId);

    int deleteBatch(List<Long> personIds);

    int insertBatch(List<Record> records);
}
