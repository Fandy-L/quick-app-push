package com.chuang.qapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
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
public class PushMsgInfDTO {

    @NotBlank(message = "快应用推送标题title不能为空！")
    @Length(min = 1,max = 20,message = "快应用推送标题长度1-20位！")
    private String title;

    @NotBlank(message = "快应用推送内容content不能为空！")
    @Length(min = 1,max = 100,message = "快应用推送标题长度1-100位！")
    private String content;

    @NotBlank(message = "快应用跳转url不能为空！")
    @Length(max = 500)
    private String url;

    @NotNull
    @Digits(fraction = 0,integer = 10,message = "消息ID长度不超过10位")
    private Integer bizMsgId;
}
