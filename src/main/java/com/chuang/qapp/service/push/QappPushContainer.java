package com.chuang.qapp.service.push;

import java.util.List;

/**
 * 厂商消息推送获取公共设备信息容器
 */
public interface QappPushContainer {

      List<String> getBatchRegIds(String flag,Integer provider,List<String> deviceIds);

      String getRegId(String flag,Integer provider,String deviceId);
}
