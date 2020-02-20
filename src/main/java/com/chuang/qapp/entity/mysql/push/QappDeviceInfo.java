package com.chuang.qapp.entity.mysql.push;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * @author fandy.lin
 */
@Data
@Entity
@Table(name = "qapp_device_info")
public class QappDeviceInfo {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "reg_id")
    private String regId;

    private Integer provider;

    private String version;

    @Column(name = "update_time")
    private Integer updateTime;

    @Column(name = "create_time")
    private Integer createTime;

    private Byte deleted;

}
