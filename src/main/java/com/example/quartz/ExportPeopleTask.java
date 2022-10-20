package com.example.quartz;

import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class ExportPeopleTask extends QuartzJobBean {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job exportPeopleJob;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        Date fireTime = jobExecutionContext.getFireTime();
        JobParametersBuilder paramBuilder = new JobParametersBuilder();
        paramBuilder.addDate("fire_time", fireTime);
        try {
            jobLauncher.run(exportPeopleJob, paramBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }
}
