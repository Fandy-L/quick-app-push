package com.chuang.qapp.entity.wapper;

import com.chuang.qapp.entity.dto.MsgOpenResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 快应用消息推送结果反馈封装
 * @author fandy.lin
 */
@Data
@Accessors(chain = true)
public class QuickAppMsgResultWrapper {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgOpenResultDTO{
        @NotNull
        @Valid
        private com.chuang.qapp.entity.dto.MsgOpenResultDTO msgOpenResult;
    }
}
