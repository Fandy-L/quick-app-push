package com.chuang.qapp.service.push;

import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.dto.SubPushMsgInfDTO;

import java.util.List;

/**
 * 快应用各厂商推送的实现接口
 * @author fandy.lin
 * 2019.11.26
 */
public interface QappPushProvider {
     /**
      * 全量推送接口
      * @param reqDTO
      * @return
      */
     QappPushResult allPush(SubPushMsgInfDTO reqDTO);

     /**
      * 批量推送接口
      *
      * @param reqDTO
      * @param deviceIds
      * @return
      */
     QappPushResult batchPush(SubPushMsgInfDTO reqDTO, List<String> deviceIds);

     /**
      * 单推接口
      *
      * @param reqDTO
      * @param deviceId
      * @return
      */
     QappPushResult singlePush(SubPushMsgInfDTO reqDTO, String deviceId);
}
