package com.example.mybatis;

import com.example.batchprocessing.Record;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecordMapper {
    int insertBatch(@Param("list") List<Record> records);
}
