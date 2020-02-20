package com.chuang.qapp.service.impl;

import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.MsgResultDTO;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.dto.SubPushMsgInfDTO;
import com.chuang.qapp.entity.mysql.push.QappMsgResult;
import com.chuang.qapp.service.AsyncTaskService;
import com.chuang.qapp.service.QappMsgPushService;
import com.chuang.qapp.service.QappMsgResultService;
import com.chuang.qapp.service.push.QappPushProvider;
import com.chuang.qapp.utils.CommonUtils;
import com.chuang.qapp.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author fandy.lin
 */
@Slf4j
@Service
public class QappMsgPushServiceImpl implements QappMsgPushService {
    @Autowired
    private AsyncTaskService asyncTaskService;
    @Autowired
    private QappMsgResultService qappMsgResultService;

    @Resource
    public QappPushProvider oppoQappPushProvider;
    @Resource
    public QappPushProvider vivoQappPushProvider;
    @Resource
    public QappPushProvider miQappPushProvider;
    @Resource
    public QappPushProvider huaweiQappPushProvider;

    @Value("${qapp.start.allow.push.time.hour:8}")
    private int startAllowPushTimeHour;

    @Value("${qapp.end.allow.push.time.hour:22}")
    private int endAllowPushTimeHour;

    @Value("${qapp.push.providers:2,3,4,5}")
    private int[] providers;

    @Override
    public void pushMessageAll(PushMsgInfDTO reqDTO) {
        this.asyncPush(reqDTO, QappMsgConstant.ALL_PUSH,null);
    }

    @Override
    public void pushMessageGroup(PushMsgInfDTO reqDTO, List<String> deviceIds) {
        this.asyncPush(reqDTO,QappMsgConstant.BATCH_PUSH,deviceIds);
    }

    @Override
    public void pushMessageSingle(PushMsgInfDTO reqDTO, String deviceId) {
        this.asyncPush(reqDTO,QappMsgConstant.SINGLE_PUSH,Arrays.asList(deviceId));
    }

    private void asyncPush(PushMsgInfDTO reqDTO,int pushType,List<String> deviceIds) {
        this.vaildateInPushTime();
        //校验表是否存在php传值bizMsgId，不存在则java生成msgId
        SubPushMsgInfDTO subReqDTO = this.buildSubPushMsgInf(reqDTO);
        asyncTaskService.submit(() -> this.asyncPush(subReqDTO,pushType,deviceIds));
    }

    private void asyncPush(SubPushMsgInfDTO reqDTO, int pushType, List<String> deviceIds) {
        Map<Integer, FutureTask<QappPushResult>> successMap = new HashMap<>(10);
        //各个厂商启动异步调用
        for (Integer provider : providers) {
            //异步开始全量推送信息
            FutureTask<QappPushResult> futureTask = new FutureTask<>(() ->{
                return this.doPush(reqDTO,provider,pushType,deviceIds);
            });
            asyncTaskService.submit(futureTask);
            //记录推送完成的future
            successMap.put(provider,futureTask);
        }
        Integer fail = 0;
        Integer success = 0;
        FutureTask<QappPushResult> task;
        QappPushResult result;
        for (Integer provider : providers){
            try{
                task = successMap.get(provider);
                result = task.get();
                success += result.getSuccessTotal();
                fail += result.getFailTotal();
                //在消息统计表新增或者更新对应厂商推送
                this.saveOrUpdatePushResult(reqDTO,result.getSuccessTotal(),pushType,provider);
            }catch (InterruptedException | ExecutionException e){
                if(e.getCause() instanceof  BizException){
                    if(pushType == QappMsgConstant.ALL_PUSH){
                        log.error("快应用多线程异步推送方式:{} 出错(ALL_PUSH = 1,BATCH_PUSH = 2,SINGLE_PUSH = 3),厂商编号:{} ,异常信息:",pushType,provider, e);
                    }else{
                        log.warn("快应用多线程异步推送方式:{} 出错(ALL_PUSH = 1,BATCH_PUSH = 2,SINGLE_PUSH = 3),厂商编号:{} ,异常信息:",pushType,provider, e);
                    }
                }else{
                    log.error("快应用多线程异步推送方式:{} 出错(ALL_PUSH = 1,BATCH_PUSH = 2,SINGLE_PUSH = 3),厂商编号:{} ,异常信息:",pushType,provider, e);
                }
                this.saveOrUpdatePushResult(reqDTO,0,pushType,provider);
            }

        }
        log.info("推送方式:{} ,总共推送{}条，推送成功{}条，推送失败{}条",pushType,success+fail,success,fail);
    }

    private void saveOrUpdatePushResult(SubPushMsgInfDTO reqDTO,Integer pushNum, int pushType, Integer provider) {
        //单推、群推成功个数为0，不保存或更新推送
        if((pushType == QappMsgConstant.SINGLE_PUSH || pushType == QappMsgConstant.BATCH_PUSH) && pushNum == 0){
            return;
        }
        //华为无推送统计接口，送达数为推送总数
        if(!provider.equals(QappMsgConstant.PROVIDER_HUAWEI)){
            this.qappMsgResultService.saveOrIncreaseMsgResultNum(MsgResultDTO.builder()
                    .msgId(reqDTO.getMsgId()).bizMsgId(reqDTO.getBizMsgId()).pushNum(pushNum).provider(provider).build());
        }else{
            this.qappMsgResultService.saveOrIncreaseMsgResultNum(MsgResultDTO.builder()
                    .msgId(reqDTO.getMsgId()).bizMsgId(reqDTO.getBizMsgId()).pushNum(pushNum).arrivedNum(pushNum).provider(provider).build());
        }
    }

    /**
     * 根据类型推送消息
     * @param reqDTO
     * @param provider
     * @param pushType
     * @param deviceIds
     * @return
     */
    private QappPushResult doPush(SubPushMsgInfDTO reqDTO,int provider,int pushType,List<String> deviceIds){
        switch (pushType){
            case QappMsgConstant.ALL_PUSH:
                return this.getPusher(provider).allPush(reqDTO);
            case QappMsgConstant.BATCH_PUSH:
                return this.getPusher(provider).batchPush(reqDTO,deviceIds);
            case QappMsgConstant.SINGLE_PUSH:
                return this.getPusher(provider).singlePush(reqDTO,deviceIds.get(0));
            default:
                throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_TYPE_ERROR);
        }
    }

    /**
     * 校验是否在允许发送消息推送的时间段
     */
    private void vaildateInPushTime() {
        int pushOfHour = DateUtils.getHour();
        if(pushOfHour >= endAllowPushTimeHour || pushOfHour < startAllowPushTimeHour){
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_TIME_LIMIT);
        }
    }

    /**
     * 获得厂商推送器
     * @param providerId
     * @return
     */
    private QappPushProvider getPusher(Integer providerId) {
        if(providerId.equals(QappMsgConstant.PROVIDER_OPPO)){
            return  this.oppoQappPushProvider;
        }else if(providerId.equals(QappMsgConstant.PROVIDER_VIVO)){
            return this.vivoQappPushProvider;
        }else if(providerId.equals(QappMsgConstant.PROVIDER_XIAOMI)){
            return this.miQappPushProvider;
        } else if(providerId.equals(QappMsgConstant.PROVIDER_HUAWEI)){
            return this.huaweiQappPushProvider;
        }else {
            log.error("快应用配置推送厂商:{}不存在",providerId);
            throw  new BizException(MyExceptionStatus.QUICK_APP_PUSH_PROVIDER_NOT_EXIST);
        }
    }

    /**
     * java根据时间戳+url+随机数生成msgId对消息进行标识
     * @param url
     * @return
     */
    private String generateMsgId(String url){
        String msgId = MessageFormat.format("{0}{1}{2}", System.currentTimeMillis(), url,Math.random());
        return CommonUtils.md5(msgId);
    }

    /**
     * 表是否存在php传值bizMsgId记录，不存在则java生成msgId与之绑定
     * @param reqDTO
     * @return
     */
    private SubPushMsgInfDTO buildSubPushMsgInf(PushMsgInfDTO reqDTO){

        List<QappMsgResult> qappMsgResult = qappMsgResultService.findQuickAppMsgResult(reqDTO.getBizMsgId());
        SubPushMsgInfDTO subPushMsgInfDTO = new SubPushMsgInfDTO(reqDTO);
        return qappMsgResult.size() > 0?
                subPushMsgInfDTO.setMsgId(qappMsgResult.get(0).getMsgId()):subPushMsgInfDTO.setMsgId(this.generateMsgId(reqDTO.getUrl()));
    }
}
