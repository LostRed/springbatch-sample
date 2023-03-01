package com.example.mybatis;

import com.example.batchprocessing.Person;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PersonMapper {
    List<Person> selectAll();
}
