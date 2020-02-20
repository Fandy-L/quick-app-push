package com.chuang.qapp.service;

import com.chuang.qapp.entity.dto.PushMsgInfDTO;

import java.util.List;

/**
 * @author fandy.lin
 */
public interface QappMsgPushService {
    /**
     * 全量推送信息给各个厂商
     * @param reqDTO
     */
    void pushMessageAll(PushMsgInfDTO reqDTO);

    /**
     * 群推信息给各个厂商
     * @param reqDTO
     * @param deviceIds
     */
    void pushMessageGroup(PushMsgInfDTO reqDTO, List<String> deviceIds);

    /**
     * 单推
     *
     * @param reqDTO
     * @param deviceId
     */
    void pushMessageSingle(PushMsgInfDTO reqDTO, String deviceId);

}
