package com.chuang.qapp.entity.mysql.push;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "qapp_push_msg_result_key")
public class QappMsgResultKey {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "msg_key")
    private String msgKey;

    private Integer provider;

    @Column(name = "provider_msg_id")
    private Integer providerMsgId;

    @Column(name = "update_time")
    private Integer updateTime;

    @Column(name = "create_time")
    private Integer createTime;

    private Byte deleted;

}
