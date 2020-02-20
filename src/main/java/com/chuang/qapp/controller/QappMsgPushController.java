package com.chuang.qapp.controller;

import com.chuang.qapp.api.QappMessagePushApi;
import com.chuang.qapp.entity.wapper.QappInfPushWrapper;
import com.chuang.qapp.entity.RespResult;
import com.chuang.qapp.service.QappMsgPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fandy.lin
 */
@RestController
public class QappMsgPushController implements QappMessagePushApi {

    @Autowired
    private QappMsgPushService qappMsgPushService;

    @Override
    public RespResult pushMessageAll(@RequestBody QappInfPushWrapper.PushMsgInfoDTO reqDTO) {
        qappMsgPushService.pushMessageAll(reqDTO.getPushMsgInf());
        return RespResult.ok();
    }

    @Override
    public RespResult pushMessageGroup(@RequestBody QappInfPushWrapper.PushMsgWithGroupDTO reqDTO) {
        qappMsgPushService.pushMessageGroup(reqDTO.getPushMsgInf(),reqDTO.getDeviceIds());
        return RespResult.ok();
    }

    @Override
    public RespResult pushMessageSingle(@RequestBody QappInfPushWrapper.PushMsgSingleDTO reqDTO) {
        qappMsgPushService.pushMessageSingle(reqDTO.getPushMsgInf(),reqDTO.getDeviceId());
        return RespResult.ok();
    }
}
