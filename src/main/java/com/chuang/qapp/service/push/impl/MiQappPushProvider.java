package com.chuang.qapp.service.push.impl;

import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.dto.SubPushMsgInfDTO;
import com.chuang.qapp.entity.mysql.push.QappPushApp;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.service.QappPushAppService;
import com.chuang.qapp.service.push.AbstractPushProvider;
import com.chuang.qapp.service.push.QappPushProvider;
import com.chuang.qapp.utils.Preconditions;
import com.chuang.qapp.utils.TimeUtils;
import com.xiaomi.push.sdk.ErrorCode;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author fandy.lin
 * Xiaomi快应用推送器
 */
@Component
@Slf4j
public class MiQappPushProvider extends AbstractPushProvider implements QappPushProvider {
    @Value("${qapp.push.resend.retries.xiaomi:3}")
    private int retries;

    @Value("${qapp.push.callback.url:http://skcjqald.xiaomy.net/quickapp/msg/callback/xiaomi}")
    private String miCallBackUrl;

    @Autowired
    public MiQappPushProvider(QappDeviceService qappDeviceService, QappPushAppService qappPushAppService) {
        QappPushApp app =  qappPushAppService.findByProvider(QappMsgConstant.PROVIDER_XIAOMI);
        Preconditions.checkNotNull(app, MyExceptionStatus.QUICK_APP_PUSH_PROVIDER_NOT_EXIST);
        this.qappDeviceService = qappDeviceService;
        this.qappPushApp = app;
    }

    @Override
    public QappPushResult allPush(SubPushMsgInfDTO reqDTO) {
        //初始化口令配置
        Sender sender = this.initMsgProvider(this.qappPushApp);
        //在vivo服务端构建消息体
        Message message = this.bulidMessage(reqDTO);
        //全量推送消息
        return this.allPush(sender,message);
    }

    @Override
    public QappPushResult batchPush(SubPushMsgInfDTO reqDTO, List<String> deviceIds) {
        List<String> regIds = this.getBatchRegIds(deviceIds);
        if(regIds.size() > 0){
            //初始化口令配置
            Sender sender = this.initMsgProvider(this.qappPushApp);
            //在vivo服务端构建消息体
            Message message = this.bulidMessage(reqDTO);
            return this.batchPush(sender,message,regIds);
        }else{
            log.info("该批量deviceId无xiaomi设备信息！");
            return buildPushResult(0,0);
        }
    }

    @Override
    public QappPushResult singlePush(SubPushMsgInfDTO reqDTO, String deviceId) {
        String regId = this.getRegId(deviceId);
        if(regId != null){
            //初始化口令配置
            Sender sender = this.initMsgProvider(this.qappPushApp);
            //在vivo服务端构建消息体
            Message message = this.bulidMessage(reqDTO);
            return  this.batchPush(sender,message,Arrays.asList(regId));
        }else {
            log.info("单推deviceId非xiaomi设备信息！");
            return buildPushResult(0,0);
        }
    }

    /**
     *初始化口令配置
     * @param QappPushApp
     * @return
     */
    private Sender initMsgProvider(QappPushApp QappPushApp) {
        Preconditions.checkNotNull(QappPushApp.getPushAppSecret(), MyExceptionStatus.QUICK_APP_PUSH_MI_APP_SECRET_NULL);
        return  new Sender(QappPushApp.getPushAppSecret());
    }

    /**全量推送消息
     * @param sender
     * @param message
     * @return
     */
    private QappPushResult allPush(Sender sender, Message message) {
        Integer batchNum = this.getTotalBatchNum();
        // 开始同步数据
        int success = 0;
        int fail = 0;
        QappPushResult result;
        List<String> regIds;
        for (int i = 0; i < batchNum; i++) {
            regIds = getBatchRegIds(i);
            result = this.batchPush(sender,message,regIds);
            success += result.getSuccessTotal();
            fail += result.getFailTotal();
            log.info("xiaomi第 {} 批次推送成功{}条，失败{}条",i+1,result.getSuccessTotal(),result.getFailTotal());
        }
        log.info("厂商编号：{} 快应用推送成功消息{}条,失败消息{}条",this.qappPushApp.getProvider(),success,fail);
        return QappPushResult.builder().successTotal(success).failTotal(fail).build();
    }

    /**
     * 批量推送消息
     */
    private QappPushResult batchPush(Sender sender, Message message, List<String> regIds) {
        Preconditions.checkNotNull(sender, MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        //批量推送消息给用户
        Result result = null;
        try {
            result = sender.sendHybridMessageByRegId(message, regIds, retries);
            if(checkIsNull(result,"xiaomi推送返回结果为空！")){
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
            }
            log.info("xiaomi批量推送返回结果:{}",result.toString());
            if(checkIsNull(result.getErrorCode(),"xiaomi推送返回码值为空！")){
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
            }
            if(ErrorCode.Success.getValue() != result.getErrorCode().getValue()){
                log.error("xiaomi快应用批量推送失败:{}-原因：{}",result.toString(),result.getReason());
                return QappPushResult.builder().successTotal(0).failTotal(regIds.size()).build();
            }
            JSONObject data = result.getData();
            if(checkIsNull(data,"xiaomi推送返回结果的data属性为空！")){
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(FAIL_ZERO).build();
            }
            if(data.get("bad_regids") != null){
                String badRegids = (String)data.get("bad_regids");
                if(checkIsNull(badRegids,"xiaomi推送返回的非法用户属性bad_regids为空！")){
                    return QappPushResult.builder().successTotal(regIds.size()).failTotal(FAIL_ZERO).build();
                }
                log.warn("xiaomi消息推送存在不合法regid:{}",this.qappPushApp.getProvider(),badRegids);
                String[] split = badRegids.split(",");
                return QappPushResult.builder().successTotal(regIds.size()-split.length).failTotal(split.length).build();
            }
            return QappPushResult.builder().successTotal(regIds.size()).failTotal(0).build();
        } catch (IOException e) {
            log.error("xiaomi快应用消息推送网络连接失败！",e);
        } catch (ParseException e) {
            log.error("xiaomi快应用消息推送解析异常！",e);
        }
        return QappPushResult.builder().successTotal(0).failTotal(regIds.size()).build();
    }


    /**
     * 构建推送消息
     */
    private Message bulidMessage(SubPushMsgInfDTO reqDTO) {
        Message message = new Message.Builder()
                .title(reqDTO.getTitle())
                .description(reqDTO.getContent())
                .restrictedPackageName(QappMsgConstant.MI_PACKAGE_NAME)
                // 使用默认提示音提示
                .notifyType(QappMsgConstant.MI_NOTIFY_TYPE)
                .extra(QappMsgConstant.MI_HYBRID_PATH, super.subUrl(reqDTO.getUrl()))
                .extra("callback",miCallBackUrl)
                .extra("callback.param",reqDTO.getMsgId())
                .extra("callback.type",QappMsgConstant.MI_CALLBACK_TYPE)
                //重复的设备value值为8位:获取时间戳秒值，转换16进制字符串
                .extra("jobkey", Integer.toHexString(TimeUtils.getCurrentTimestamp()))
                .build();
        log.info("xiaomi推送消息:{}",message.toString());
        return  message;
    }

}
