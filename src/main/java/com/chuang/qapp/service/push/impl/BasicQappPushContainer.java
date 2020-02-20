package com.chuang.qapp.service.push.impl;

import com.chuang.qapp.entity.mysql.push.QappDeviceInfo;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.service.push.QappPushContainer;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author  fandy.lin
 * 待测试：解决各厂商单推、批量推 均查询一次数据库问题
 */
@Component
public class BasicQappPushContainer implements QappPushContainer {
    @Autowired
    private QappDeviceService qappDeviceService;
    private volatile Map<String,Map<Integer,List<String>>> batchContainer =  new HashMap<String,Map<Integer,List<String>>>();
    private volatile Map<String,Integer> providerCountMap = new HashMap<>();

    @Override
    public List<String> getBatchRegIds(String flag,Integer provider, List<String> deviceIds){
        if(batchContainer.get(flag) == null){
            synchronized (this){
                if(batchContainer.get(flag) == null){
                    Integer providerCount = 0;
                    List<QappDeviceInfo> qappDeviceInfos = qappDeviceService.findByDeviceIds(deviceIds);
                    Map<Integer, List<String>> providerMap = new Hashtable<>();
                    for(QappDeviceInfo info:qappDeviceInfos){
                        List<String> list = providerMap.get(info.getProvider());
                        if(list != null){
                            list.add(info.getDeviceId());
                        }else{
                            providerCount += 1;
                            list = new ArrayList<>();
                            providerMap.put(info.getProvider(),list);
                        }
                    }
                    providerCountMap.put(flag,providerCount);
                }
            }
        }
        return this.getBatchRegIds(flag,provider);
    }

    private List<String> getBatchRegIds(String flag,Integer provider){
        Map<Integer, List<String>> providerMap = batchContainer.get(flag);
        List<String> list = providerMap.get(provider);
        if(list != null){
            synchronized (this){
                int providerCount = providerCountMap.get(flag);
                providerCount -= 1;
                if(providerCount == 0){
                    batchContainer.remove(flag);
                    providerMap.remove(flag);
                }else{
                    providerCountMap.put(flag,providerCount);
                }
            }
        }
        return list;
    }

    @Override
    public String getRegId(String flag, Integer provider,String deviceId) {
        return null;
    }

}
