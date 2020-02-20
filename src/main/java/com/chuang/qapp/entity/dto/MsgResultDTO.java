package com.chuang.qapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fandy.lin
 * 快应用推送信息封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgResultDTO {
    /**
     * php传值业务消息id
      */
    private int bizMsgId;
    /**
     * java维护消息id
     */
    private String msgId;
    /**
     * 推送总数
     */
    private int pushNum;
    /**
     * 送达数
     */
    private int arrivedNum;
    /**
     * 打开数（前端反馈统计）
     */
    private int openNum;
    /**
     * 打开数（厂商反馈统计）
     */
    private int providerOpenNum;
    /**
     * 厂商编号
     */
    private int provider;
}
