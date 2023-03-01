package com.example.mybatis;

import com.example.batchprocessing.Person;

import java.util.List;

public interface PersonMapper {
   List<Person> selectAll();
}
