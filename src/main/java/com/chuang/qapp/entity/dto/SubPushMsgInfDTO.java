package com.chuang.qapp.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

/**
 * @author fandy.lin
 * 快应用推送子类信息封装
 */
@Data
@Accessors(chain = true)
public class SubPushMsgInfDTO extends PushMsgInfDTO{

    @NotNull
    @Digits(fraction = 0,integer = 10,message = "java生成消息Id长度不超过32位")
    String msgId;

    public SubPushMsgInfDTO(PushMsgInfDTO pushMsgInfDTO) {
        this.setBizMsgId(pushMsgInfDTO.getBizMsgId());
        this.setContent(pushMsgInfDTO.getContent());
        this.setTitle(pushMsgInfDTO.getTitle());
        this.setUrl(pushMsgInfDTO.getUrl());
    }
}
