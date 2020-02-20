package com.chuang.qapp.service.push.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
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
import com.chuang.qapp.service.push.entity.HuaweiMessage;
import com.chuang.qapp.utils.HttpUtils;
import com.chuang.qapp.utils.Preconditions;
import com.chuang.qapp.utils.RedisOperations;
import com.chuang.qapp.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fandy.lin
 * Huawei快应用推送器
 */
@Component
@Slf4j
public class HuaweiQappPushProvider extends AbstractPushProvider implements QappPushProvider {
    private String pushUrl;
    //全部regid推送成功
    private static final Integer SUCCESS_CODE = 80000000;
    //部分regid推送成功
    private static final Integer REGID_HAVE_INVALID_CODE = 80100000;
    //所有regid推送不成功
    private static final Integer REGID_ALL_INVALID_CODE = 80300007;
    @Value(("${qapp.push.token.expire.time.huawei:10}"))
    private int tokenExpireTime;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private DistributedLocker locker;

    @Autowired
    public HuaweiQappPushProvider(QappDeviceService qappDeviceService, QappPushAppService qappPushAppService, RedisOperations redisOperations) {
        QappPushApp app = qappPushAppService.findByProvider(QappMsgConstant.PROVIDER_HUAWEI);
        Preconditions.checkNotNull(app, MyExceptionStatus.QUICK_APP_PUSH_PROVIDER_NOT_EXIST);
        this.qappDeviceService = qappDeviceService;
        this.qappPushApp = app;
        this.redisOperations = redisOperations;
        //获取推送url
        this.pushUrl = this.getPushUrl();
    }

    @Override
    public QappPushResult allPush(SubPushMsgInfDTO reqDTO) {
        //获取推送口令
        String accessToken = this.getAccessToken();
        //构建huawei消息体
        String message = this.bulidHuaweiMessage(reqDTO);
        //全量推送消息
        return this.allPush(accessToken, message);
    }

    @Override
    public QappPushResult batchPush(SubPushMsgInfDTO reqDTO, List<String> deviceIds) {
        List<String> regIds = this.getBatchRegIds(deviceIds);
        if (regIds.size() > 0) {
            //获取推送口令
            String accessToken = this.getAccessToken();
            //构建huawei消息体
            String message = this.bulidHuaweiMessage(reqDTO);
            //生成推送离线时间
            String expireTime = this.generateExpireTime();
            return this.batchPush(accessToken, expireTime, message, regIds);
        } else {
            log.info("该批量deviceId无huawei设备信息！");
            return buildPushResult(0, 0);
        }
    }

    @Override
    public QappPushResult singlePush(SubPushMsgInfDTO reqDTO, String deviceId) {
        String regId = this.getRegId(deviceId);
        if (regId != null) {
            //获取推送口令
            String accessToken = this.getAccessToken();
            //构建huawei消息体
            String message = this.bulidHuaweiMessage(reqDTO);
            //生成推送离线时间
            String expireTime = this.generateExpireTime();
            return this.batchPush(accessToken, expireTime, message, Arrays.asList(regId));
        } else {
            log.info("单推deviceId非huawei设备信息！");
            return buildPushResult(0, 0);
        }
    }


    /**
     * 构建推送消息
     */
    private String bulidHuaweiMessage(SubPushMsgInfDTO reqDTO) {
        String subUrl = super.subUrl(reqDTO.getUrl());
        String page = super.subPage(subUrl);
        Map<String, Object> paramMap = super.getUrlParamMap(subUrl);
        HuaweiMessage messageEntity = new HuaweiMessage(reqDTO.getTitle(), reqDTO.getContent(), page,
                paramMap);
        String message = JSON.toJSONString(messageEntity);
        log.info("huawei推送json消息:{}", message);
        return message;
    }

    /**
     * 全量推送消息
     *
     * @param accessToken
     * @param message
     * @return
     */
    private QappPushResult allPush(String accessToken, String message) {
        Integer batchNum = this.getTotalBatchNum();
        //生成推送离线时间
        String expireTime = this.generateExpireTime();
        // 开始同步数据
        int success = 0;
        int fail = 0;
        QappPushResult result;
        List<String> regIds;
        for (int i = 0; i < batchNum; i++) {
            regIds = getBatchRegIds(i);
            result = this.batchPush(accessToken, expireTime, message, regIds);
            success += result.getSuccessTotal();
            fail += result.getFailTotal();
            log.info("huawei第 {} 批次推送成功{}条，失败{}条", i + 1, result.getSuccessTotal(), result.getFailTotal());
        }
        log.info("厂商编号：{} 快应用推送成功消息{}条,失败消息{}条", this.qappPushApp.getProvider(), success, fail);
        return QappPushResult.builder().successTotal(success).failTotal(fail).build();
    }

    /**
     * 批量推送消息
     */
    private QappPushResult batchPush(String accessToken, String expireTime, String message, List<String> regIds) {
        //服务请求时间戳。单位秒。（传入的时间与服务器时间相差5分钟以上，服务器可能会拒绝请求）
        int nspTs = TimeUtils.getCurrentTimestamp() + QappMsgConstant.HUAWEI_REQEST_TIMESTAMP;
        String deviceTokenList = JSON.toJSONString(regIds);
        String requestBody = MessageFormat.format("access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&expire_time={4}&payload={5}",
                accessToken, "openpush.message.api.send", nspTs, deviceTokenList, expireTime, message);
        log.info("huawei推送请求报文体：{}", requestBody);
        String jsonResultStr = null;
        try {

            jsonResultStr = httpUtils.doPost(this.pushUrl, requestBody);

            if (checkIsNull(jsonResultStr, "huawei批量消息推送返回结果为空！")) {
                return this.buildPushResult(SUCCESS_ZERO, regIds.size());
            }
            log.info("huawei推送返回结果：{}", jsonResultStr);
            JSONObject jsonResult = JSONObject.parseObject(jsonResultStr);
            Integer code = jsonResult.getInteger("code");
            if (checkIsNull(code, "huawei返回推送消息状态码为空,josnResult:{}", jsonResultStr)) {

                log.warn("huawei快应用消息推送返回结果异常:{}", jsonResultStr);
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
            } else {
                //非正常返回返回码值，直接返回,正常返回码分别表示：全部推送成功，部分推送成功,全部推送不成功
                if (!(SUCCESS_CODE.equals(code) || REGID_HAVE_INVALID_CODE.equals(code) || REGID_ALL_INVALID_CODE.equals(code))) {
                    log.warn("huawei快应用消息推送返回结果异常:{}", jsonResultStr);

                    return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
                }
            }
            String msg = jsonResult.getString("msg");
            if (checkIsNull(msg, "huawei返回推送消息msg属性为空！")) {
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
            }
            if (msg.equals("Success")) {
                return QappPushResult.builder().successTotal(regIds.size()).failTotal(FAIL_ZERO).build();
            } else if (msg.equals("All the tokens are invalid")) {
                log.warn("huawei消息推送存在不合法regid:{}", JSON.toJSONString(regIds));
                return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
            } else {
                JSONObject msgResult = JSONObject.parseObject(msg);
                Integer success = msgResult.getInteger("success");
                Integer failure = msgResult.getInteger("failure");
                if (checkIsNull(success, "huawei返回推送成功用户解析异常！")) {
                    success = 0;
                }
                if (checkIsNull(success, "huawei返回推送失败用户解析异常！")) {
                    failure = 0;
                }
                log.warn("huawei消息推送存在不合法regid:{}", msg);
                return QappPushResult.builder().successTotal(success).failTotal(failure).build();
            }
        } catch (JSONException e) {

            log.warn("huawei快应用消息推送返回结果:{},解析异常!", jsonResultStr, e);
        } catch (NumberFormatException e) {
            log.warn("huawei快应用消息推送返回结果:{},数据类型转换解析异常!", jsonResultStr, e);
        } catch (Exception e) {
            log.error("huawei快应用批量推送异常！", e);
        }
        return QappPushResult.builder().successTotal(SUCCESS_ZERO).failTotal(regIds.size()).build();
    }

    /**
     * 获得本快应用推送路径
     *
     * @return
     */
    private String getPushUrl() {
        JSONObject urlParams = new JSONObject();
        urlParams.put("ver", "1");
        urlParams.put("appId", this.qappPushApp.getPushAppId());
        String nspCtx = JSON.toJSONString(urlParams);
        try {
            nspCtx = URLEncoder.encode(nspCtx, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("huawei推送进行nsp_ctx url encoding异常！", e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_URL_ENCODING_ERROR, e);
        }
        String reqUrl = MessageFormat.format("{0}?nsp_ctx={1}", QappMsgConstant.HUAWEI_PUSH_URL, nspCtx);
        log.info("huawei推送路径：{}", reqUrl);
        return reqUrl;
    }

    /**
     * 获得离线时间
     *
     * @return
     */
    private String generateExpireTime() {
        String expireTime = TimeUtils.getISO8601Timestamp(new Date(System.currentTimeMillis() + QappMsgConstant.HUAWEI_EXPIRE_TIME));
        try {
            expireTime = URLEncoder.encode(expireTime, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("huawei推送进行expireTime url encoding异常！", e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_URL_ENCODING_ERROR, e);
        }
        return expireTime;
    }

    /**
     * 获取推送口令
     * @return
     */
    private String getAccessToken() {
        long timeStamp = 0;
        try {
            //查询token是否存在缓存
            String token = redisOperations.getString(QappMsgConstant.HUAWEI_REDIS_CACHE_TOKEN_KEY);
            if (token == null) {
                //获取分布式锁
                log.info("huawei快应用线程开始竞争锁");
                // 获取锁：等待锁时间、锁过期时间、单位
                if (locker.tryLock(QappMsgConstant.HUAWEI_REDIS_TOKEN_LOCK_KEY, TimeUnit.MILLISECONDS,lockWaitTime, lockExpireTime)) {
                    timeStamp = System.currentTimeMillis();
                    //重新获得token
                    token = redisOperations.getString(QappMsgConstant.HUAWEI_REDIS_CACHE_TOKEN_KEY);
                    if (token == null) {
                        token = this.getHuaweiAccessToken();
                    }
                    log.info("huawei快应用push获取token:{}", token);
                    redisOperations.set(QappMsgConstant.HUAWEI_REDIS_CACHE_TOKEN_KEY, token, tokenExpireTime, TimeUnit.MINUTES);
                }
            }
            if (checkIsNull(token, "huawei快应用分布式锁下未能获取到token!")) {
                throw new BizException(MyExceptionStatus.QUICK_APP_DISTRIBUTE_LOCK_GAIN_TOKEN_ERR);
            }
            return token;
        } finally {
            if (locker != null) {
                log.info("huawei快应用释放分布式锁！");
                locker.unlock(QappMsgConstant.HUAWEI_REDIS_TOKEN_LOCK_KEY);
                if (timeStamp != 0) {
                    log.info("huawei快应用占用分布式锁时间:{}ms", System.currentTimeMillis() - timeStamp);
                }
            }
        }
    }

    private String getHuaweiAccessToken() {
        String requestBody = MessageFormat.format("grant_type=client_credentials&client_secret={0}&client_id={1}",
                qappPushApp.getPushAppSecret(), qappPushApp.getPushAppId());
        try {

            String jsonResult = httpUtils.doPost(QappMsgConstant.HUAWEI_ACCESS_TOCKEN_URL, requestBody);
            Preconditions.checkNotNull(jsonResult, MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_GET_TOKEN_NULL);
            log.info("huawei获取aceestoken返回结果：{}", jsonResult);
            JSONObject jsonObject = JSONObject.parseObject(jsonResult);
            Preconditions.checkNotNull(jsonObject, MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_GET_TOKEN_NULL);
            String accessToken = jsonObject.getString("access_token");
            Preconditions.checkNotNull(accessToken, MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_TOKEN_NULL);
            accessToken = URLEncoder.encode(accessToken, "utf-8");
            return accessToken;
        } catch (IOException e) {

            log.warn("huawei快应用获取token出错！", e);
            throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_HUAWEI_CONN_FAIL, e);
        }
    }

}
