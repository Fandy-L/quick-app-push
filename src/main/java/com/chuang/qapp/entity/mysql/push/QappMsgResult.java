package com.chuang.qapp.entity.mysql.push;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "qapp_msg_push_result")
public class QappMsgResult {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "biz_msg_id")
    private Integer bizMsgId;

    @Column(name = "msg_id")
    private String msgId;

    private Integer provider;

    @Column(name = "push_num")
    private Integer pushNum;

    @Column(name = "arrived_num")
    private Integer arrivedNum;

    @Column(name = "open_num")
    private Integer openNum;

    @Column(name = "provider_open_num")
    private Integer providerOpenNum;

    @Column(name = "update_time")
    private Integer updateTime;

    @Column(name = "create_time")
    private Integer createTime;

    private Byte deleted;

}
