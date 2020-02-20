package com.chuang.qapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author fandy.lin
 * 快应用推送信息封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgOpenResultDTO {

    @NotBlank(message = "消息id不能为空！")
    @Length(min = 32,max = 32,message = "消息id长度32位！")
    private String msgId;

    @NotNull(message = "厂商标识不能为空！厂商：华为-2，小米-3，OPPO-4，VIVO-5，魅族-6")
    @Max(value = 15,message = "厂商标识超长！")
    private Integer provider;

    @NotBlank(message = "设备唯一标识deviceId不能为空！")
    @Length(max = 36,message = "设备唯一标识deviceId长度不大于36位！")
    private String deviceId;

    @NotBlank(message = "设备品牌不能为空！")
    @Length(max = 36,message = "设备品牌不能大于36位！")
    private String trademark;
}
