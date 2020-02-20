package com.chuang.qapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.service.AbstractQuickAppMsgService;
import com.chuang.qapp.service.QappMsgCallbackService;
import com.chuang.qapp.utils.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @author fandy.lin
 */
@Slf4j
@Service
public class QappMsgCallbackServiceImpl extends AbstractQuickAppMsgService implements QappMsgCallbackService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${qapp.push.result.watch.message:true}")
    private boolean watchMessage;

    @Override
    public void dealXiaomiMsgCallBack(Map<String, String> params) {
        //xiaomi消息json串
        String dataStr = params.get("data");
        //配置是否需要查看报文详情
        if(watchMessage) {log.info("xiaomi回执报文：{}",dataStr);}
        Preconditions.checkNotNull(dataStr, MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR,dataStr);
        JSONObject dataJsonObject = JSON.parseObject(dataStr);
        int type;
        String msgId;
        String cacheKey;
        String[] regIds;
        for(String key:dataJsonObject.keySet()){
            try{
                JSONObject data = dataJsonObject.getJSONObject(key);
                //获得消息统计类型
                type = data.getInteger("type");
                //获取透传消息id
                msgId = data.getString("param");
                //获取regid列表
                regIds = this.splitRegIds(data.getString("targets"), QappMsgConstant.PROVIDER_XIAOMI,dataStr);
                //统计到达数
                if(type == QappMsgConstant.MI_TYPE_ARRIVED){
                    //将消息id装成缓存key
                    cacheKey = MessageFormat.format("{0}_{1}_{2}", msgId, QappMsgConstant.PROVIDER_XIAOMI, QappMsgConstant.ARRIVED_SUFFIX);
                    //将regId存储redis计数
                    this.addToHyperLogLog(cacheKey,regIds);
                //统计点击数
                }else if(type == QappMsgConstant.MI_TYPE_OPEN){
                    //将消息id装成缓存key
                    cacheKey = MessageFormat.format("{0}_{1}_{2}", msgId, QappMsgConstant.PROVIDER_XIAOMI, QappMsgConstant.PROVIDER_OPEN_SUFFIX);
                    //将regId存储redis计数
                    this.addToSet(cacheKey,regIds);
                    //获得到达数统计
                    long setSize = this.getSetSize(cacheKey);
                    log.info("xiaomi hyperLogLog点击数统计计数大小:{}",setSize);
                }else{
                    log.warn("xiaomi消息统计类型非到达数、点击数!回执报文:{}",dataStr);
                }
            }catch (JSONException e){
                log.warn("xiaomi推送回执报文：{} ,解析异常！",dataStr,e);
                throw  new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR,e);
            }
        }
    }

    @Override
    public void dealOppoMsgCallBack(List<Map<String, String>> params) {
        //配置是否需要查看报文详情
        try {
            String dataStr = JSON.toJSONString(params);
            if (watchMessage) {
                log.info("oppo回执报文：{}", dataStr);
            }
            String cacheKey;
            String msgId;
            String[] regIds;
            for (Map<String, String> data : params) {
                //消息回传参数为msgId
                msgId = data.get("param");
                Preconditions.checkNotNull(msgId, MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR, dataStr);
                //将消息id装成缓存key
                cacheKey = MessageFormat.format("{0}_{1}_{2}", msgId, QappMsgConstant.PROVIDER_OPPO, QappMsgConstant.ARRIVED_SUFFIX);
                //获取到达数regid列表
                regIds = this.splitRegIds(data.get("registrationIds"), QappMsgConstant.PROVIDER_OPPO, dataStr);
                this.addToHyperLogLog(cacheKey, regIds);
            }
        }catch (JSONException e)  {
            log.warn("oppo回执解析异常！",e);
            throw  new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR,e);
        }
    }

    @Override
    public void dealVivoMsgCallBack(String params) {
        //配置是否需要查看报文详情
        if(watchMessage) {log.info("vivo回执报文：{}",params);}
        try {
            JSONObject dataJsonObject = JSON.parseObject(params);
            String cacheKey;
            String msgId;
            JSONObject data;
            String[] regIds;
            for(String key:dataJsonObject.keySet()){
                data = dataJsonObject.getJSONObject(key);
                msgId = data.getString("param");
                Preconditions.checkNotNull(msgId,MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR,params);
                //将消息id装成缓存key
                cacheKey = MessageFormat.format("{0}_{1}_{2}", msgId, QappMsgConstant.PROVIDER_VIVO, QappMsgConstant.ARRIVED_SUFFIX);
                regIds = this.splitRegIds(data.getString("targets"),QappMsgConstant.PROVIDER_VIVO,params);
                this.addToHyperLogLog(cacheKey,regIds);
            }
        }catch (JSONException e){
            log.warn("vivo推送回执报文：{} ,解析异常！",params,e);
            throw  new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR,e);
        }
    }

    private String[] splitRegIds(String targets,int provider,String callBackData){
        if(targets != null){
           String[] regIds = targets.split(",");
           return  regIds;
        }else{
            log.warn("厂商回执，编号：{} ,回执报文格式异常:{}",provider,callBackData);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR);
        }
    }
}
