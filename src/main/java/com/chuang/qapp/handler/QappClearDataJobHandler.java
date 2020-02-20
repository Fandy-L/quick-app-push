package com.chuang.qapp.handler;

import com.chuang.qapp.service.QappDeviceService;
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.handler.IJobHandler;
//import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 快应用过期设备信息清理定时任务调度
 * @author fandy.lin
 */
@Component
@Slf4j
public class QappClearDataJobHandler implements Job {

    @Autowired
    private QappDeviceService qappDeviceService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            log.info("开始清理一个月未更新的快应用设备信息！");
            long startTime = System.currentTimeMillis();
            qappDeviceService.removeDevInfOfMonth();
            log.info("完成清理一个月未更新的快应用设备信息耗时:{}",System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("启动定时调度任务失败！");
        }
    }
}
