package com.chuang.qapp.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fandy.lin
 * 快应用推送信息封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgResultVO implements Serializable {
    private Integer bizMsgId;
    private Long pushNum;
    private Long arrivedNum;
    private Long openNum;

}
