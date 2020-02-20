package com.chuang.qapp.common;

import ch.qos.logback.classic.Level;
import com.chuang.qapp.compatible.Status;

/**
 * @author fandy.lin
 */
public enum MyExceptionStatus implements Status {
    /**
     * 操作成功的状态码
     */
    PARAMS_CONTAINS_NULL("参数为空",10020,Level.WARN),
    QUICK_APP_DEVINF_DUP("多个设备出现相同diviceId", 30002,Level.ERROR),
    QUICK_APP_DEVINF_CLEAR_ERR("清除快应用设备信息异常", 30003,Level.ERROR),
    QUICK_APP_PUSH_MSG_OPPO_LINK_ERR("与厂商oppo建立推送链接异常！",30004,Level.ERROR),
    QUICK_APP_PUSH_MSG_INIT("请初始化快应用推送配置！",30005,Level.ERROR),
    QUICK_APP_PUSH_MSG_OPPO_ERR("oppo发送批量单推消息异常！",30006,Level.ERROR),
    QUICK_APP_PUSH_MSG_DEV_ZERO("厂商设备信息不存在于数据库！",30007,Level.WARN),
    QUICK_APP_PUSH_PROVIDER_NOT_EXIST("快应用推送厂商不存在！",30008,Level.ERROR),
    QUICK_APP_PUSH_PAGE_SIZE_ZERO("快应用全量推送每批次大小不能为0！",30009,Level.ERROR),
    QUICK_APP_PUSH_VIVO_TASK_ID_NULL("快应用vivo批量推送，获得消息taskId为空！",30010,Level.ERROR),
    QUICK_APP_PUSH_MI_APP_SECRET_NULL("快应用xiaomi的appSecret不存在数据库！",30011,Level.ERROR),
    QUICK_APP_PUSH_CONN_FAIL("消息推送网络连接失败！",30012,Level.ERROR),
    QUICK_APP_PUSH_HUAWEI_CONN_FAIL("huawei获取token出错！",30013,Level.ERROR),
    QUICK_APP_PUSH_HUAWEI_TOKEN_NULL("huawei获取token为空值！",30014,Level.ERROR),
    QUICK_APP_PUSH_HUAWEI_URL_ENCODING_ERROR("huawei推送进行url encoding异常！",30015,Level.ERROR),
    QUICK_APP_PUSH_MSG_VIVO_LINK_ERR("与厂商vivo建立推送链接异常！",30016,Level.ERROR),
    QUICK_APP_PUSH_MSG_VIVO_TOKEN_PARSE_ERR("vivo获取token返回结果或解析异常！",30017,Level.ERROR),
    QUICK_APP_PUSH_MSG_HUAWEI_RESULT_NULL("华为消息推送返回结果为空！",30018,Level.ERROR),
    QUICK_APP_PUSH_MSG_VIVO_UPPER_LIMIT("vivo消息推送条数超出订阅数上限！",30019,Level.ERROR),
    QUICK_APP_PUSH_HUAWEI_GET_TOKEN_NULL("huawei获取aceestoken返回结果为空值！",30020,Level.ERROR),
    QUICK_APP_PUSH_VIVO_PUSH_RESULT_TASK_ID_NULL("快应用vivo批量推送，获得消息taskId为结果空！",30021,Level.ERROR),
    QUICK_APP_PUSH_VIVO_PUSH_RESULT_NULL("快应用vivo批量推送返回结果空！",30022,Level.ERROR),
    QUICK_APP_PUSH_VIVO_PUSH_RESULT_TASK_ID_FAIL("vivo群推消息,taskId创建请求失败！",30023,Level.ERROR),
    QUICK_APP_PUSH_RESULT_JSON_DATA_ERROR("厂商回执报文格式异常!",30024,Level.WARN),
    QUICK_APP_PUSH_TYPE_ERROR("厂商回执报文格式异常!",30025,Level.ERROR),
    QUICK_APP_PUSH_RESULT_VIVO_MSG_ID_MISS("vivo消息统计的msgId不存在!",30026,Level.ERROR),
    QUICK_APP_PUSH_MSG_TIME_LIMIT("消息推送时间不在可推送时间范围内!",30027,Level.WARN),
    QUICK_APP_PUSH_RESULT_MSG_ID_MISS("vivo消息统计的msgId不存在!",30028,Level.ERROR),
    QUICK_APP_DEVICE_REGIDS_INVALID("快应用设备信息存在非法regid!",30029,Level.ERROR),
    QUICK_APP_GAIN_DISTRIBUTE_LOCK_ERR("快应用设备信息存在非法regid!",30030,Level.ERROR),
    QUICK_APP_DISTRIBUTE_LOCK_GAIN_TOKEN_ERR("快应用分布式锁下未能获取到token!",30031,Level.ERROR),
    QUICK_APP_VIVO_STATISTICS_LINK_ERR("vivo快应用消息统计响应报文网络异常!",30031,Level.ERROR),

    ;

    private String msg;
    private int code;
    private Level logLevel = Level.ERROR;

    MyExceptionStatus(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    MyExceptionStatus(String msg, int code, Level lv) {
        this.msg = msg;
        this.code = code;
        this.logLevel = lv;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.msg;
    }

    @Override
    public Level getLogLevel() {
        return this.logLevel;
    }
}
