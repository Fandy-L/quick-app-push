package com.chuang.qapp.service.push.impl;

import com.alibaba.fastjson.JSON;
import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.compatible.DistributedLocker;
import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.dto.SubPushMsgInfDTO;
import com.chuang.qapp.entity.mysql.push.QappPushApp;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.service.QappPushAppService;
import com.chuang.qapp.service.push.AbstractPushProvider;
import com.chuang.qapp.service.push.QappPushProvider;
import com.chuang.qapp.service.push.QappPushSetter;
import com.chuang.qapp.utils.Preconditions;
import com.chuang.qapp.utils.RedisOperations;
import com.vivo.push.sdk.notofication.InvalidUser;
import com.vivo.push.sdk.notofication.Message;
import com.vivo.push.sdk.notofication.Result;
import com.vivo.push.sdk.notofication.TargetMessage;
import com.vivo.push.sdk.server.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fandy.lin
 * vivo快应用推送器
 */
@Component
@Slf4j
public class VivoQappPushProvider extends AbstractPushProvider implements QappPushProvider,
        QappPushSetter {

    private final static int REGIDLENGTH = 23;
    //vivo推送超出总量限制的错误码
    private final static int PUSH_LIMIT_CODE = 10070;
    private int SUCCESS_CODE = 0;

    @Autowired
    private DistributedLocker locker;

    //单位：分钟
    @Value(("${qapp.push.token.expire.time.vivo:10}"))
    private int tokenExpireTime;

    @Value("${qapp.push.callback.url:http://skcjqald.xiaomy.net/quickapp/msg/callback/vivo}")
    private String vivoCallBackUrl;

    @Autowired
    public VivoQappPushProvider(QappDeviceService qappDeviceService, QappPushAppService QappPushAppService, RedisOperations redisOperations) {
        QappPushApp app = QappPushAppService.findByProvider(QappMsgConstant.PROVIDER_VIVO);
        Preconditions.checkNotNull(app, MyExceptionStatus.QUICK_APP_PUSH_PROVIDER_NOT_EXIST);
        this.qappDeviceService = qappDeviceService;
        this.qappPushApp = app;
        this.redisOperations = redisOperations;
    }

    @Override
    public QappPushResult allPush(SubPushMsgInfDTO reqDTO) {
        //初始化口令配置
        Sender sender = this.initMsgProvider(this.qappPushApp);
        //在vivo服务端构建消息体
        String taskId = this.createBatchMessage(sender, reqDTO);
        Preconditions.checkNotNull(taskId, MyExceptionStatus.QUICK_APP_PUSH_VIVO_TASK_ID_NULL);
        //全量推送消息
        return this.allPush(sender,taskId,reqDTO);
    }

    @Override
    public QappPushResult batchPush(SubPushMsgInfDTO reqDTO, List<String> deviceIds) {
        List<String> regIds = this.getBatchRegIds(deviceIds);
        if(regIds.size() > 0){
            Sender sender = this.initMsgProvider(this.qappPushApp);
            String taskId = this.createBatchMessage(sender,reqDTO);
            Preconditions.checkNotNull(taskId,MyExceptionStatus.QUICK_APP_PUSH_VIVO_TASK_ID_NULL);
            return this.batchPush(sender,taskId,regIds,reqDTO);
        }else{
            log.info("该批量deviceId无vivo设备信息！");
            return buildPushResult(0,0);
        }
    }

    @Override
    public QappPushResult singlePush(SubPushMsgInfDTO reqDTO, String deviceId) {
        String regId = this.getRegId(deviceId);
        if(regId != null){
            Sender sender = this.initMsgProvider(this.qappPushApp);
            return this.singlePush(sender,reqDTO,regId);
        }else {
            log.info("单推deviceId非vivo设备信息！");
            return buildPushResult(0,0);
        }
    }

    /**
     *初始化口令配置
     * @param QappPushApp
     * @return
     */
    private Sender initMsgProvider(QappPushApp QappPushApp) {
        long timeStamp = 0;
        try {
            Sender sender = new Sender(QappPushApp.getPushAppSecret());
            //查询token是否存在缓存
            String token = redisOperations.getString(QappMsgConstant.VIVO_REDIS_CACHE_TOKEN_KEY);
            if (token == null) {
                //获取分布式锁
                log.info("vivo快应用线程开始竞争锁.");
                // 获取锁：等待锁时间、锁过期时间、单位
                if(locker.tryLock(QappMsgConstant.VIVO_REDIS_TOKEN_LOCK_KEY,TimeUnit.MILLISECONDS,lockWaitTime,lockExpireTime)){
                    timeStamp = System.currentTimeMillis();
                    //重新获得token
                    token = redisOperations.getString(QappMsgConstant.VIVO_REDIS_CACHE_TOKEN_KEY);
                    if (token == null){
                        Result result = sender.getToken(Integer.parseInt(this.qappPushApp.getPushAppId()), this.qappPushApp.getPushAppKey());
                        if(checkIsNull(result,"vivo获取token返回结果为空！result:{}",result.toString())){
                            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_TOKEN_PARSE_ERR);
                        }
                        token = result.getAuthToken();
                        if(checkIsNull(token,"vivo获取token返回结果解析异常 result：{}",result.toString())){
                            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_TOKEN_PARSE_ERR);
                        }
                        log.info("vivo获取token结果:{}",token);
                        redisOperations.set(QappMsgConstant.VIVO_REDIS_CACHE_TOKEN_KEY, token, tokenExpireTime, TimeUnit.MINUTES);
                    }
                }
            }
            if(checkIsNull(token,"vivo快应用分布式锁下未能获取到token!")){
                throw  new BizException(MyExceptionStatus.QUICK_APP_DISTRIBUTE_LOCK_GAIN_TOKEN_ERR);
            }
            sender.setAuthToken(token);
            return sender;
        }catch (IOException e){
            log.warn("vivo快应用获取token返回结果解析异常！",e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_TOKEN_PARSE_ERR,e.getMessage());
        }catch (InterruptedException e){
            log.warn("vivo获取分布式锁异常！",e);
            throw new BizException(MyExceptionStatus.QUICK_APP_GAIN_DISTRIBUTE_LOCK_ERR,e.getMessage());
        }catch (Exception e) {
            log.warn("与厂商vivo快应用建立推送链接异常!",e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_LINK_ERR,e.getMessage());
        }finally {
            if(locker != null){
                log.info("vivo快应用释放分布式锁！");
                locker.unlock(QappMsgConstant.VIVO_REDIS_TOKEN_LOCK_KEY);
                if(timeStamp != 0){
                    log.info("vivo快应用占用分布式锁时间:{}ms",System.currentTimeMillis()-timeStamp);
                }
            }
        }
    }

    /**全量推送消息
     * @param sender
     * @param taskId
     * @param reqDTO
     * @return
     */
    private QappPushResult allPush(Sender sender, String taskId, SubPushMsgInfDTO reqDTO) {
        Integer batchNum = this.getTotalBatchNum();
        // 开始同步数据
        int success = 0;
        int fail = 0;
        QappPushResult result;
        List<String> regIds;
        for (int i = 0; i < batchNum; i++) {
            regIds = getBatchRegIds(i);
            result = this.batchPush(sender, taskId, regIds,reqDTO);
            success += result.getSuccessTotal();
            fail += result.getFailTotal();
            log.info("vivo第 {} 批次推送成功{}条，失败{}条",i+1,result.getSuccessTotal(),result.getFailTotal());
        }
        log.info("厂商编号：{} 快应用推送成功消息{}条,失败消息{}条", this.qappPushApp.getProvider(), success, fail);
        return QappPushResult.builder().successTotal(success).failTotal(fail).build();
    }

    /**
     * 批量推送消息
     */
    private QappPushResult batchPush(Sender sender, String taskId, List<String> regIds, SubPushMsgInfDTO reqDTO) {
        Preconditions.checkNotNull(sender, MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        //构建批量推送用户群
        Set<String> batch = new HashSet<>(QappMsgConstant.PAGE_SIZE);
        batch.addAll(regIds);
        Result result;
        TargetMessage target = null;
        try {
            target =  new TargetMessage.Builder().taskId(taskId).regIds(batch).requestId(UUID.randomUUID().toString()).build();
        }catch (IllegalArgumentException e){
            //对不合法的regid进行过滤后重推
            Iterator<String> iterator = batch.iterator();
            while (iterator.hasNext()){
                String regId = iterator.next();
                if (regId.length() != REGIDLENGTH){
                    log.warn("厂商编号:{} ,vivo快应用消息推送存在不合法regId:{}",this.qappPushApp.getProvider(),regId);
                    iterator.remove();
                }
            }
            if (batch.size() > 1) {
                target = new TargetMessage.Builder().taskId(taskId).regIds(batch).requestId(UUID.randomUUID().toString()).build();
            }
        }
        if (batch.size() > 1) {
            try {
                result = sender.sendToList(target);
                log.info("vivo批量推送的返回结果：{}", result.toString());
                if(checkIsNull(result,"vivo批量推送的返回结果为空！")){
                    throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_VIVO_PUSH_RESULT_NULL);
                }
                if(result.getResult() != SUCCESS_CODE){
                    log.warn("vivo快应用批量推送消息失败：{}",result.toString());
                    if(result.getResult() == PUSH_LIMIT_CODE){
                        throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_UPPER_LIMIT);
                    }
                }
                int fail = 0;
                if(result.getInvalidUsers() != null) {
                    for (InvalidUser invalidUser : result.getInvalidUsers()) {
                        log.warn("vivo快应用消息推送失败，用户id:{}-描述status：{}", invalidUser.getUserid(), invalidUser.getStatus());
                        fail++;
                    }
                }
                return QappPushResult.builder().failTotal(fail).successTotal(regIds.size() - fail).build();
            }catch (IOException e){
                log.warn("vivo快应用群推消息网络异常！", e);
            }catch (Exception e) {
                log.error("vivo群推消息返回结果异常！", e);
            }
        }else if (batch.size() == 1){
            String regId = (String) batch.toArray()[0];
            log.info("vivo批量推送只推送了一条，regId:{}",regId);
            return this.singlePush(sender,reqDTO, regId);
        }else{
            log.warn("vivo快应用批量推送去除不合法regid后，推送regid为空！");
        }
        return QappPushResult.builder().successTotal(0).failTotal(regIds.size()).build();
    }

    /**
     * 单条推送消息
     */
    private QappPushResult singlePush(Sender sender, SubPushMsgInfDTO reqDTO, String regId) {
        Preconditions.checkNotNull(sender, MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        //该测试手机设备订阅推送后生成的regId
        Message singleMessage = new Message.Builder()
                .regId(regId)
                .notifyType(QappMsgConstant.VIVO_NOTIFY_TYPE_2)
                .title(reqDTO.getTitle())
                .content(reqDTO.getContent())
                .timeToLive(QappMsgConstant.VIVO_TIME_TO_LIVE)
                .skipType(QappMsgConstant.VIVO_SKIP_TYPE)
                .skipContent(reqDTO.getUrl())
                .networkType(QappMsgConstant.VIVO_NETWORK_TYPE)
                .extra(vivoCallBackUrl,reqDTO.getMsgId())
                .requestId(UUID.randomUUID().toString())
                .build();
        log.info("vivo单推的生成信息：{}",JSON.toJSONString(singleMessage));
        try {
            Result resultMessage = sender.sendSingle(singleMessage);
            log.info("vivo单推的返回结果：{}",resultMessage.toString());
            if(checkIsNull(resultMessage,"vivo单推的返回结果为空！")){
                throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_VIVO_PUSH_RESULT_NULL);
            }
            if(SUCCESS_CODE != resultMessage.getResult()){
                InvalidUser invalidUser = resultMessage.getInvalidUser();
                if (invalidUser != null) {
                    log.warn("vivo快应用单推消息失败，用户id:{}-描述status：{}", invalidUser.getUserid(), invalidUser.getStatus());
                }else{
                    log.warn("vivo快应用单推消息失败:{}",resultMessage.toString());
                    if(resultMessage.getResult() == PUSH_LIMIT_CODE){
                        throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_VIVO_UPPER_LIMIT);
                    }
                }
            }else{
                return QappPushResult.builder().failTotal(0).successTotal(1).build();
            }
        }catch (IllegalArgumentException e){
            log.warn("vivo快应用推送参数不合法,regId:{},message:{}.",regId,JSON.toJSONString(singleMessage),e);
            throw new BizException(e.getMessage());
        }catch (IOException e){
            log.warn("vivo快应用单推消息网络异常！",e);
        }catch (Exception e) {
            log.error("vivo快应用单推消息返回结果异常！",e);
        }
        return QappPushResult.builder().successTotal(0).failTotal(1).build();

    }

    /**
     * 生成 批量推送消息
     */
    private String createBatchMessage(Sender sender, SubPushMsgInfDTO reqDTO) {
        Message message = new Message.Builder()
                .notifyType(QappMsgConstant.VIVO_NOTIFY_TYPE_2)
                .title(reqDTO.getTitle())
                .content(reqDTO.getContent())
                .timeToLive(QappMsgConstant.VIVO_TIME_TO_LIVE)
                .skipType(QappMsgConstant.VIVO_SKIP_TYPE)
                .skipContent(reqDTO.getUrl())
                .networkType(QappMsgConstant.VIVO_NETWORK_TYPE)
                .extra(vivoCallBackUrl,reqDTO.getMsgId())
                .requestId(UUID.randomUUID().toString())
                .build();
        try {
            log.info("vivo批量推送消息生成为：{}", JSON.toJSONString(message));
            //发送保存群推消息请求
            Result result = sender.saveListPayLoad(message);
            if(checkIsNull(result,"vivo批量推送生成消息taskId异常返回结果为空！")){
                Preconditions.checkNotNull(result,MyExceptionStatus.QUICK_APP_PUSH_VIVO_PUSH_RESULT_TASK_ID_NULL);
            }
            log.info("vivo发送保存群推消息请求结果：{}",result.toString());
            if(0 != result.getResult()){
                log.warn("vivo快应用群推消息，消息创建请求失败,状态描述:{}-原因:{}",result.getResult(),result.getDesc());
                throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_VIVO_PUSH_RESULT_TASK_ID_FAIL);
            }
            return result.getTaskId();
        } catch (Exception e) {

            log.warn("vivo快应用群推保存消息异常!");
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_VIVO_PUSH_RESULT_TASK_ID_NULL);
        }
    }

    @Override
    public String getToken() {
        String token = redisOperations.getString(QappMsgConstant.VIVO_REDIS_CACHE_TOKEN_KEY);
        if(token == null){
            initMsgProvider(this.qappPushApp);
            token = redisOperations.getString(QappMsgConstant.VIVO_REDIS_CACHE_TOKEN_KEY);
        }
        return token;
    }
}
