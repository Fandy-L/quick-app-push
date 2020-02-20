package com.chuang.qapp.service.push;

import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.mysql.push.QappDeviceInfo;
import com.chuang.qapp.entity.mysql.push.QappPushApp;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.utils.RedisOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fandy.lin
 * 快应用推送抽象类
 */
@Slf4j
public abstract class AbstractPushProvider {
    protected RedisOperations redisOperations;
    protected QappDeviceService qappDeviceService;
    protected QappPushApp qappPushApp;
    protected int SUCCESS_ZERO = 0;
    protected int FAIL_ZERO = 0;


    @Value("${qapp.push.url.domain.pattern:hap://app/com.xxx.xxx}")
    private String urlPattern;

    //分布式锁过期时间,单位：毫秒
    @Value("${qapp.push.lock.expire.time:3000}")
    protected int lockExpireTime;
    //等待获取锁时间
    @Value("${qapp.push.lock.expire.time:3000}")
    protected int lockWaitTime;

    protected List<String> groupFilter(List<String> deviceIds){
        ArrayList<String> regIds = new ArrayList<>();
        List<QappDeviceInfo> infs = qappDeviceService.findByDeviceIds(deviceIds);
        for(QappDeviceInfo inf:infs){
            if(qappPushApp.getProvider().equals(inf.getProvider())){
                regIds.add(inf.getRegId());
            }
        }
        return regIds;
    }

    protected Integer getTotalBatchNum(){
        Integer total = qappDeviceService.countByProvider(qappPushApp.getProvider());
        if(total.intValue() == 0){
            log.warn("厂商编号：{}，设备信息不存在于数据库", QappMsgConstant.PROVIDER_OPPO);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_DEV_ZERO);
        }
        if(QappMsgConstant.PAGE_SIZE < 1){
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_PAGE_SIZE_ZERO);
        }
        int batchNum = total / QappMsgConstant.PAGE_SIZE;
        int batchNumOver = total % QappMsgConstant.PAGE_SIZE;
        batchNum = batchNumOver == 0 ? batchNum : batchNum+1;
        log.info("厂商编号：{} 快应用消息推送开始，总条数：{},分批推送总页数:{}", qappPushApp.getProvider(),total,batchNum);
        return batchNum;
    }

    protected   List<String> getBatchRegIds(Integer pageNum){
        List<QappDeviceInfo> infos = qappDeviceService.findAllDevInfByProvider(qappPushApp.getProvider(),
                pageNum, QappMsgConstant.PAGE_SIZE);
        return infos.stream().map(QappDeviceInfo::getRegId).collect(Collectors.toList());
    }

    protected String subUrl(String url){
        String subUrl = url.substring(urlPattern.length());
        log.info("url的包下相对路径为：{}",subUrl);
        return subUrl;
    }

    protected String subPage(String subUrl){
        int i = subUrl.indexOf("?");
        if (i == -1){
            return subUrl;
        }else{
            String page = subUrl.substring(0, subUrl.indexOf("?"));
            log.info("url下的页面地址：{}",page);
            return page;
        }
    }

    protected Map<String,Object> getUrlParamMap(String subUrl){
        HashMap<String, Object>  params = new HashMap<>(8);
        int i = subUrl.indexOf("?");
        if (i == -1) {
            params.put("","");
            return params;
        }else{
            String paramsStr = subUrl.substring(i+1);
            String[] paramList = paramsStr.split("&");
            for(String param:paramList){
                String[] splitParam = param.split("=");
                if(splitParam.length != 2){
                    log.error("url传参错误,该url:{}",subUrl);
                    return new HashMap<>();
                }
                params.put(splitParam[0],splitParam[1]);
            }
            return params;
        }
    }

    protected List<String> getBatchRegIds(List<String> deviceIds){
        return qappDeviceService.findRegIdsByProviderAndDeviceId(qappPushApp.getProvider(),deviceIds);
    }

    protected String getRegId(String deviceId){
        return qappDeviceService.findRegIdByProviderAndDeviceId(qappPushApp.getProvider(),deviceId);
    }

    protected QappPushResult buildPushResult(int success, int fail){
        return QappPushResult.builder().successTotal(success).failTotal(fail).build();
    }

    /**
     * 非空判断
     * @return
     */
    protected boolean checkIsNull(Object o,String logMsg,String... info){
        if(o != null){
            return false;
        }else{
            if(info!=null){
                log.warn(logMsg,info);
            }else{
                log.warn(logMsg);
            }
            return true;
        }
    }
}
