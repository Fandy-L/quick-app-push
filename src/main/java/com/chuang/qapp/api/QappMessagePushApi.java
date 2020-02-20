package com.chuang.qapp.api;

import com.chuang.qapp.entity.wapper.QappInfPushWrapper;

import com.chuang.qapp.entity.RespResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author fandy.lin
 * 快应用信息推送push
 */

@Validated
public interface QappMessagePushApi {

    /**
     * 全量推送消息
     * @param reqDTO
     * @return
     */
    @PostMapping(value = "/qapp/msg/allpush")
    RespResult pushMessageAll(@RequestBody @Valid QappInfPushWrapper.PushMsgInfoDTO reqDTO);

    /**
     * 群推消息
     * @param reqDTO
     * @return
     */
    @PostMapping(value = "/qapp/msg/grouppush")
    RespResult pushMessageGroup(@RequestBody @Valid QappInfPushWrapper.PushMsgWithGroupDTO reqDTO);

    /**
     * 单推消息
     * @param reqDTO
     * @return
     */
    @PostMapping(value = "/qapp/msg/singlepush")
    RespResult pushMessageSingle(@RequestBody @Valid QappInfPushWrapper.PushMsgSingleDTO reqDTO);
}
