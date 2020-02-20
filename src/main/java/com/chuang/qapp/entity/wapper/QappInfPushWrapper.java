package com.chuang.qapp.entity.wapper;

import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 快应用推送内容封装
 * @author fandy.lin
 */
@Data
@Accessors(chain = true)
public class QappInfPushWrapper {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushMsgInfoDTO{
        @NotNull
        @Valid
        private PushMsgInfDTO pushMsgInf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushMsgWithGroupDTO{
        @NotEmpty
        @Valid
        List<@Length(max = 36,message = "设备唯一标识deviceId长度不大于36位！") String> deviceIds;
        @NotNull
        @Valid
        private PushMsgInfDTO pushMsgInf;
    }

    @Data
    public static class PushMsgSingleDTO{
        @NotBlank
        @Length(max = 36,message = "设备唯一标识deviceId长度不大于36位！")
        private String deviceId;

        @NotNull
        @Valid
        private PushMsgInfDTO pushMsgInf;
    }

}
