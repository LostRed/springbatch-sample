package com.example.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class JobController {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job importPeopleJob;
    @Autowired
    private Job exportPeopleJob;

    @GetMapping("/importPeopleJob/run")
    public String importPeopleJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParametersBuilder paramBuilder = new JobParametersBuilder();
        paramBuilder.addDate("running_time", new Date());
        jobLauncher.run(importPeopleJob, paramBuilder.toJobParameters());
        return "ok";
    }

    @GetMapping("/exportPeopleJob/run")
    public String exportPeopleJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParametersBuilder paramBuilder = new JobParametersBuilder();
        paramBuilder.addDate("running_time", new Date());
        jobLauncher.run(exportPeopleJob, paramBuilder.toJobParameters());
        return "ok";
    }
}
