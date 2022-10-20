package com.example.quartz;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class QuartzController {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Scheduler scheduler;

    @GetMapping("/create")
    public String create(String beanName, String cron) {
        QuartzJobBean bean = applicationContext.getBean(beanName, QuartzJobBean.class);
        //构建job信息
        JobDetail jobDetail = JobBuilder.newJob(bean.getClass())
                .withIdentity(JobKey.jobKey(beanName))
                .build();
        //表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionDoNothing();
        //按新的cronExpression表达式构建一个新的trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(beanName))
                .withSchedule(scheduleBuilder)
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.pauseJob(JobKey.jobKey(beanName));
            return "ok";
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/resume")
    public String resume(String beanName) {
        try {
            scheduler.resumeJob(JobKey.jobKey(beanName));
            return "ok";
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/pause")
    public String pause(String beanName) {
        try {
            scheduler.pauseJob(JobKey.jobKey(beanName));
            return "ok";
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/execute")
    public String execute(String beanName) {
        try {
            scheduler.triggerJob(JobKey.jobKey(beanName));
            return "ok";
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
