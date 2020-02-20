package com.chuang.qapp.entity.wapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 快应用设备保存接收参数封装
 * @author fandy.lin
 */
@Data
@Accessors(chain = true)
public class QappDevInfWrapper {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceDetailInfDTO{
        @NotBlank(message = "设备唯一标识deviceId不能为空！")
        @Length(max = 36,message = "设备唯一标识deviceId长度不大于36位！")
        private String deviceId;


        @NotBlank(message = "注册id不能为空！")
        @Length(max = 150,message = "注册id长度不大于150位！")
        private String regId;

        @NotNull(message = "厂商标识不能为空！")
        @Max(value = 15,message = "厂商标识超长！")
        private Integer provider;


        @NotBlank(message = "应用版本号不能为空！")
        @Length(max = 11,message = "版本号长度不能超出11位！")
        @Pattern(regexp = "^[1-9]{1}\\d{0,2}\\.\\d{1,3}\\.\\d{1,3}$",message = "版本号不符合格式要求！（如：7.10.7）")
        private String version;
    }


}
