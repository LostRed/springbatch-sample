package com.example.quartz;

import com.example.quartz.ExportPeopleTask;
import org.quartz.*;

public class QuartzConfiguration {
    public JobDetail jobDetail() {
        return JobBuilder.newJob(ExportPeopleTask.class)
                .withIdentity("exportPeopleJob", "exportPeopleJob")
                .storeDurably()
                .build();
    }

    public Trigger trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity("exportPeopleJob", "exportPeopleJob")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();
    }
}
