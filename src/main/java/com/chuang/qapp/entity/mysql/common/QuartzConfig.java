package com.chuang.qapp.entity.mysql.common;
 
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * fandy.lin
 */
@Data
@Entity
@Table(name = "quartz_config")
public class QuartzConfig {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "job_class_name")
    private String jobClassName;

    @Column(name = "job_key")
    private String jobKey;

    private String cron;

    @Column(name = "update_time")
    private Integer updateTime;

    @Column(name = "create_time")
    private Integer createTime;

    private Integer deleted;
}