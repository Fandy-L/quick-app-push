package com.chuang.qapp.entity.mysql.push;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * @author fandy.lin
 */
@Data
@Entity
@Table(name = "qapp_push_app")
public class QappPushApp {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    private Integer provider;

    @Column(name = "push_app_id")
    private String pushAppId;

    @Column(name = "push_app_key")
    private String pushAppKey;

    @Column(name = "push_app_secret")
    private String pushAppSecret;

    @Column(name = "push_master_secret")
    private String masterSecret;

    private String name;

    @Column(name = "package_name")
    private String packageName;

    private Integer enable;

    @Column(name = "update_time")
    private Integer updateTime;

    @Column(name = "create_time")
    private Integer createTime;

    private Integer deleted;

}
