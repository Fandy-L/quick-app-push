package com.chuang.qapp.service;

import com.chuang.qapp.entity.mysql.common.QuartzConfig;

import java.util.List;

/**
 * @author fandy.lin
 * 任务调度更新服务
 */
public interface QuartzScheduleService {
    List<QuartzConfig> findQuartzConfig();
}
