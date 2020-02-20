package com.chuang.qapp.service.push.impl;

import com.alibaba.fastjson.JSON;
import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.dto.SubPushMsgInfDTO;
import com.chuang.qapp.entity.mysql.push.QappPushApp;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.service.QappPushAppService;
import com.chuang.qapp.service.push.AbstractPushProvider;
import com.chuang.qapp.service.push.QappPushProvider;
import com.chuang.qapp.utils.Preconditions;
import com.oppo.push.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLHandshakeException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author fandy.lin
 * oppo快应用推送器
 */
@Component
@Slf4j
public class OppoQappPushProvider extends AbstractPushProvider implements QappPushProvider {
    private static final Pattern PATTERN_OBJECT_ID = Pattern.compile("[0-9a-fA-F]{24}");
    private static final Pattern PATTERN_REGISTRATION_ID = Pattern.compile("((([0-9A-Za-z]{2,10})_)?([A-Z]{2}|EUEX)_)?[0-9a-fA-F]{32}");
    private int SUCCESS_CODE = 0;
    private int DAILY_LIMIT_CODE = 33;
    @Value("${qapp.push.connection.retries.huawei:3}")
    private int connectionRetries;
    //重试获取链接，休眠毫秒数
    @Value("${qapp.push.connection.retries.sleep.time:300}")
    private int sleepTime;
    @Value("${qapp.push.callback.url:http://skcjqald.xiaomy.net/quickapp/msg/callback/oppo}")
    private String oppoCallBackUrl;

    @Autowired
    public OppoQappPushProvider(QappDeviceService qappDeviceService, QappPushAppService qappPushAppService) {
        QappPushApp app = qappPushAppService.findByProvider(QappMsgConstant.PROVIDER_OPPO);
        Preconditions.checkNotNull(app, MyExceptionStatus.QUICK_APP_PUSH_PROVIDER_NOT_EXIST);
        this.qappDeviceService = qappDeviceService;
        this.qappPushApp = app;
    }

    @Override
    public QappPushResult allPush(SubPushMsgInfDTO reqDTO) {
        //初始化口令配置
        Sender sender = this.initMsgProvider(this.qappPushApp);
        //生成消息
        Notification notification = this.buildNotification(reqDTO);
        //全量推送消息
        return this.allPush(sender,notification);
    }

    @Override
    public QappPushResult batchPush(SubPushMsgInfDTO reqDTO, List<String> deviceIds) {
        List<String> regIds = this.getBatchRegIds(deviceIds);
        if(regIds.size() > 0){
            //初始化口令配置
            Sender sender = this.initMsgProvider(this.qappPushApp);
            //生成消息
            Notification notification = this.buildNotification(reqDTO);
            return this.batchPush(sender,notification,regIds);
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
            Notification notification = this.buildNotification(reqDTO);
            return  this.singlePush(sender,notification,regId);
        }else {
            log.info("单推deviceId非oppo设备信息！");
            return buildPushResult(0,0);
        }
    }

    /**
     *初始化口令配置
     * @param QappPushApp
     * @return
     */
    private Sender initMsgProvider(QappPushApp QappPushApp) {

        Sender sender = null;
        for(int i = 0;i <= connectionRetries;i++){
            try {
                sender = new Sender(QappPushApp.getPushAppKey(),QappPushApp.getMasterSecret());
                break;
            }catch (SSLHandshakeException e){
                log.warn("与oppo建立快应用推送链接SSL握手异常！",e);
            } catch (Exception e) {
                log.warn(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR.getMessage(),e);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
            }
        }
        if(sender != null){
            return sender;
        }else{
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        }

    }

    /**全量推送消息
     * @param sender
     * @param notification
     * @return
     */
    private QappPushResult allPush(Sender sender, Notification notification) {
        Integer batchNum = this.getTotalBatchNum();
        // 开始同步数据
        int success = 0;
        int fail = 0;
        QappPushResult result;
        List<String> regIds;
        for (int i = 0; i < batchNum; i++) {
            regIds = getBatchRegIds(i);
            result = this.batchPush(sender,notification,regIds);
            success += result.getSuccessTotal();
            fail += result.getFailTotal();
            log.info("oppo第 {} 批次推送成功{}条，失败{}条",i+1,result.getSuccessTotal(),result.getFailTotal());
        }
        log.info("厂商编号：{} 快应用推送成功消息{}条,失败消息{}条",this.qappPushApp.getProvider(),success,fail);
        return QappPushResult.builder().successTotal(success).failTotal(fail).build();
    }

    /**
     * 批量推送消息
     */
    private QappPushResult batchPush(Sender sender, Notification notification, List<String> regIds) {
        Preconditions.checkNotNull(sender, MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        // batch最大为1000
        Map<Target, Notification> batch = new HashMap<>(QappMsgConstant.PAGE_SIZE);
        for(String regId:regIds){
            batch.put(Target.build(regId), notification);
        }
        //发送批量单推消息
        Result result = null;
        try {
            result = sender.unicastBatchNotification(batch);
        }catch (IllegalArgumentException e){
            //对不合法regids进行排查
            Set<Map.Entry<Target, Notification>> entries = batch.entrySet();
            Iterator<Map.Entry<Target, Notification>> iterator = entries.iterator();
            while (iterator.hasNext()){
                Map.Entry<Target, Notification> entity = iterator.next();
                String regId = entity.getKey().getTargetValue();
                //不满足校验条件进行过滤
                if(!(regId !=null && (PATTERN_REGISTRATION_ID.matcher(regId).matches() || PATTERN_OBJECT_ID.matcher(regId).matches()))){
                    log.warn("厂商编号：{} ,oppo消息推送存在不合法regid:{}",this.qappPushApp.getProvider(),regId);
                    iterator.remove();
                }
            }
            //重新推送
            if(batch.size() >0){
                try {
                    result = sender.unicastBatchNotification(batch);
                } catch (Exception ex) {
                    log.error(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_ERR.getMessage(),ex);
                    return QappPushResult.builder().failTotal(regIds.size()).successTotal(0).build();
                }
            }else{
                return QappPushResult.builder().failTotal(regIds.size()).successTotal(0).build();
            }
        }catch (Exception e) {
            log.error(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_ERR.getMessage(),e);
            return QappPushResult.builder().failTotal(regIds.size()).successTotal(0).build();
        }
        log.info("oppo快应用批量推送返回结果：{}",result.toString());
        //批量单推结果
        if(checkIsNull(result.getReturnCode(),"oppo单推返回码值为空！")){
            return QappPushResult.builder().failTotal(regIds.size()).successTotal(SUCCESS_ZERO).build();
        }
        if(result.getReturnCode().getCode() != SUCCESS_CODE){
            if(result.getReturnCode().getCode() == DAILY_LIMIT_CODE){
                log.warn("oppo快应用推送达到上限！");
            }
            log.warn("oppo快应用批量推送返回结果异常:{}",result.toString());
            return QappPushResult.builder().failTotal(regIds.size()).successTotal(SUCCESS_ZERO).build();
        }
        List<Result.UnicastBatchResult> batchResult = result.getUnicastBatchResults();
        if(batchResult == null) {
            log.warn("厂商编号：{},根据异常返回结果,oppo快应用本批次推送的batch:{},regIds:{}", qappPushApp.getProvider(),JSON.toJSONString(batch),JSON.toJSONString(regIds));
            return QappPushResult.builder().failTotal(regIds.size()).successTotal(0).build();
        }else{
            int fail = 0;
            for (Result.UnicastBatchResult record : batchResult) {
                if (record.getErrorCode() != null) {
                    log.error("OPPO快应用推送出错：messageId:{}-regId:{}-errorMessage:{}-", record.getMessageId(), record.getTargetValue(), record.getErrorMessage());
                    fail++;
                }
            }
            return QappPushResult.builder().failTotal(fail).successTotal(regIds.size()-fail).build();
        }
    }

    private QappPushResult singlePush(Sender sender,Notification notification,String regId){
        Preconditions.checkNotNull(sender,MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_LINK_ERR);
        Target target = Target.build(regId);
        //发送单推信息
        Result result;
        try {
            result = sender.unicastNotification(notification,target);
            log.info("oppo单推返回:{}",result.toString());
        } catch (Exception e) {
            log.warn("oppo快应用单推target:{},返回结果异常!",JSON.toJSONString(target),e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_MSG_OPPO_ERR,e.getMessage());
        }
        if(checkIsNull(result.getReturnCode(),"oppo单推返回码值为空！")){
            return QappPushResult.builder().failTotal(1).successTotal(SUCCESS_ZERO).build();
        }
        if (result.getReturnCode().getCode() == 0) {
            return QappPushResult.builder().failTotal(FAIL_ZERO).successTotal(1).build();
        }else{
            if(result.getReturnCode().getCode() == DAILY_LIMIT_CODE){
                log.warn("oppo快应用推送达到上限！");
            }
            log.warn("oppo快应用单推返回信息异常：{},发送消息为:{},regid:{}",JSON.toJSONString(result),JSON.toJSONString(notification),regId);
            return QappPushResult.builder().failTotal(1).successTotal(SUCCESS_ZERO).build();
        }
    }

    /**
     * 生成消息
     */
    private Notification buildNotification(SubPushMsgInfDTO reqDTO) {
        Notification notification = new Notification();
        //以下参数必填项
        notification.setTitle(reqDTO.getTitle());
        notification.setContent(reqDTO.getContent());


        // 以下参数非必填项， 如果需要使用可以参考OPPO push服务端api文档进行设置
        //subTitle对title进行补充说明，oppo支持人员建议不填
        //notification.setSubTitle(reqDTO.getTitle());

        //通知栏样式 1. 标准样式  2. 长文本样式  3. 大图样式 【非必填，默认1-标准样式】
        notification.setStyle(QappMsgConstant.OPPO_STYLE_1);

        // App开发者自定义消息Id，OPPO推送平台根据此ID做去重处理，对于广播推送相同appMessageId只会保存一次，对于单推相同appMessageId只会推送一次
        // notification.setAppMessageId(UUID.randomUUID().toString());

        // 应用接收消息到达回执的回调URL，字数限制200以内，中英文均以一个计算
        //notification.setCallBackUrl("http://www.baidu.com");

        // App开发者自定义回执参数，字数限制50以内，中英文均以一个计算
        // notification.setCallBackParameter("");

        // 点击动作类型0，启动应用；1，打开应用内页（activity的intent action）；2，打开网页；4，打开应用内页（activity）；【非必填，默认值为0】;5,Intent scheme URL
        notification.setClickActionType(QappMsgConstant.OPPO_CLICK_TYPE_2);

        // 应用内页地址【click_action_type为1或4时必填，长度500】
        //notification.setClickActionActivity("com.coloros.push.demo.component.InternalActivity");

        // 网页地址【click_action_type为2必填，长度500】
        notification.setClickActionUrl(reqDTO.getUrl());

        // 动作参数，打开应用内页或网页时传递给应用或网页【JSON格式，非必填】，字符数不能超过4K，示例：{"key1":"value1","key2":"value2"}
        //notification.setActionParameters("{\"key1\":\"value1\",\"key2\":\"value2\"}");

        // 展示类型 (0, “即时”),(1, “定时”)
        notification.setShowTimeType(QappMsgConstant.OPPO_SHOW_TIME_TYPE_0);

        // 定时展示开始时间（根据time_zone转换成当地时间），时间的毫秒数
        //notification.setShowStartTime(System.currentTimeMillis() + 1000 * 60 * 3);

        // 定时展示结束时间（根据time_zone转换成当地时间），时间的毫秒数
        //notification.setShowEndTime(System.currentTimeMillis() + 1000 * 60 * 5);

        // 是否进离线消息,【非必填，默认为True】
        notification.setOffLine(true);

        // 离线消息的存活时间(time_to_live) (单位：秒), 【off_line值为true时，必填，最长10天】
        notification.setOffLineTtl(QappMsgConstant.OPPO_OFF_LINE_TOL);

        // 时区，默认值：（GMT+08:00）北京，香港，新加坡
        notification.setTimeZone(QappMsgConstant.TIME_ZONE);

        // 0：不限联网方式, 1：仅wifi推送
        notification.setNetworkType(QappMsgConstant.OPPO_NETWOKE_TYPE_0);

        //andriod8.0以上必输，不然快应用客户端接收不到信息
        notification.setChannelId(QappMsgConstant.OPPO_CHANNEL_ID);

        notification.setCallBackUrl(oppoCallBackUrl);
        notification.setCallBackParameter(reqDTO.getMsgId());

        log.info("OPPO推送的消息生成为：{}",notification.toString());
        return notification;
    }
}
