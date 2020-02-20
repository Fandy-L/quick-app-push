package com.chuang.qapp.api;

import com.chuang.qapp.entity.RespResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

/**
 * @author fandy.lin
 */

@Validated
public interface QappMsgCallbackApi {

    /**
     * xiaomi消息回执接口
     * @param params
     * @return
     */
    @PostMapping(value = "/qapp/msg/callback/xiaomi")
    RespResult xiaomiMsgCallBack(Map<String, String> params);

    /**
     * oppo消息回执接口
     * @param params
     * @return
     */
    @PostMapping(value = "/qapp/msg/callback/oppo")
    RespResult oppoMsgCallBack(List<Map<String, String>> params);

    /**
     * vivo消息回执接口
     * @param params
     * @return
     */
    @PostMapping(value = "/qapp/msg/callback/vivo")
    RespResult vivoMsgCallBack(String params);

}
