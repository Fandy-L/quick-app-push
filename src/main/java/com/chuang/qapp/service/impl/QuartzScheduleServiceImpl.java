package com.chuang.qapp.service.impl;

import com.chuang.qapp.entity.mysql.common.QuartzConfig;
import com.chuang.qapp.handler.QappClearDataJobHandler;
import com.chuang.qapp.repository.mysql.common.QuartzRepository;
import com.chuang.qapp.service.QuartzScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fandy.lin
 * 任务调度更新服务
 */
@Slf4j
@EnableScheduling
@Component
public class QuartzScheduleServiceImpl implements QuartzScheduleService {

    @Autowired
    private QuartzRepository quartzRepository;

    private Scheduler scheduler;

    CronTriggerFactoryBean cronTriggerFactoryBean;

    private static final int FIXED_RATE = 30000 ;

    @Autowired
    public QuartzScheduleServiceImpl(SchedulerFactoryBean schedulerFactoryBean) {
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        scheduler = schedulerFactoryBean.getScheduler();
        cronTriggerFactoryBean = new CronTriggerFactoryBean();
    }

    /**
     *每隔30s查库，并根据查询结果决定是否重新设置定时任务
     */
    @Scheduled(fixedRate = FIXED_RATE)
    public void scheduleUpdateCronTrigger() throws SchedulerException {
        List<QuartzConfig> quartzConfigs = this.findQuartzConfig();
        CronTrigger trigger = null;
        String searchCron;
        TriggerKey triggerKey;
        for(QuartzConfig quartzConfig:quartzConfigs){
            triggerKey = new TriggerKey(quartzConfig.getJobKey());
            trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            searchCron = quartzConfig.getCron();
            if(trigger != null){
                if (!searchCron.equals(trigger.getCronExpression())) {
                    //表达式调度构建器
                    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(searchCron);
                    //按新的cronExpression表达式重新构建trigger
                    trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule
                            (scheduleBuilder).build();
                    // 按新的trigger重新设置job执行
                    scheduler.rescheduleJob(triggerKey, trigger);
                }
            }else {
                JobDetail jobDetail = null;
                try {
                    jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(quartzConfig.getJobClassName())) .withIdentity(quartzConfig.getJobKey()).build();
                } catch (ClassNotFoundException e) {
                    log.error("定时调度任务加载类出错，className:{}",quartzConfig.getJobClassName());
                }
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(searchCron);
                CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(quartzConfig.getJobKey()).withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail,cronTrigger);
            }
        }
    }

    @Override
    public List<QuartzConfig> findQuartzConfig(){
        return this.quartzRepository.findAll();
    }


}
