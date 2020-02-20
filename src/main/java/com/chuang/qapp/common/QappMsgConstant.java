package com.chuang.qapp.common;


import java.util.Arrays;
import java.util.List;

/**
 * @author fandy.lin
 */
public class QappMsgConstant {
    /**
     * 厂商类型
     */
    public static final  int PROVIDER_HUAWEI = 2;
    public static final  int PROVIDER_XIAOMI = 3;
    public static final  int PROVIDER_OPPO = 4;
    public static final  int PROVIDER_VIVO = 5;
    public static final  List<Integer> PROVIDERS = Arrays.asList(new Integer[]{PROVIDER_HUAWEI,PROVIDER_OPPO,PROVIDER_VIVO,PROVIDER_XIAOMI});

    /**
     * 厂商消息推送方式
     */
    public static final  int ALL_PUSH = 1;
    public static final  int BATCH_PUSH = 2;
    public static final  int SINGLE_PUSH = 3;

    /**
     * push允许的时间范围
     */
    public  static  final  int START_ALLOW_PUSH_TIME_HOUR = 8;
    public  static  final  int END_ALLOW_PUSH_TIME_HOUR = 22;

    /**
     * 快应用url请求头格式（小米消息推送需要去掉url该请求头，类似于java类只需填classPath路径）
     */
    public  static  final  String HEAD_URL_PATTERN = "hap://app/com.tubatu.demo";
    public  static  final  int HEAD_URL_PATTERN_LENGTH = HEAD_URL_PATTERN.length();

    //redis token存储的时长，官方文档推荐按 vivo 3-4小时合适，单位：分钟
    public static final int VIVO_TOKEN_EXPIRE_TIME = 60*3;
    //redis token存储 华为1小时，这里设置50分钟 单位：分钟
    public static final int HUAWEI_TOKEN_EXPIRE_TIME = 50;
    //redis锁失效时间10秒，单位：秒
    public static final int LOCK_EXPIRE_TIME = 10;
    //锁获取失败，重新获得redis锁的线程休眠时间 单位：毫秒
    public static final int REGAIN_LOCK_SLEEP_TIME = 100;
    //redis 存储的vivo tokenKey
    public static final String VIVO_REDIS_CACHE_TOKEN_KEY = "quick_app_token_vivo";
    //redis 存储的vivo token锁key
    public static final String VIVO_REDIS_TOKEN_LOCK_KEY = "quick_app_token_lock_vivo";
    //redis 存储的huawei tokenKey
    public static final String HUAWEI_REDIS_CACHE_TOKEN_KEY = "quick_app_token_huawei";
    //redis 存储的huawei token锁key
    public static final String HUAWEI_REDIS_TOKEN_LOCK_KEY = "quick_app_token_lock_huawei";

    /**
     * push消息统计
     */
    //redis存储后缀
    public static final String OPEN_SUFFIX = "open_num";
    public static final String PROVIDER_OPEN_SUFFIX = "provider_open_num";
    public static final String ARRIVED_SUFFIX = "arrived_num";
    //redis统计消息存储时长,单位：秒
    public static final int PUSH_RESULT_EXPIRE_TIME = 3*24*3600;


    /**
     * 分页条数
     */
    public static final int PAGE_SIZE = 100;
    //public static final int PAGE_SIZE = 3;

    /**
     * oppo推送设置
     */
    //通知栏样式 1. 标准样式  2. 长文本样式  3. 大图样式 【非必填，默认1-标准样式】
    public static final int OPPO_STYLE_1 = 1;
    //oppo点击类型
    public static final int OPPO_CLICK_TYPE_2 = 2;
    //展示类型 (0, “即时”),(1, “定时”)
    public static final int OPPO_SHOW_TIME_TYPE_0 = 0;
    //离线消息的存活时间
    public static final int OPPO_OFF_LINE_TOL = 3*24*3600;
    // 时区，默认值：（GMT+08:00）北京，香港，新加坡
    public static final String TIME_ZONE = "GMT+08:00";
    // 0：不限联网方式, 1：仅wifi推送
    public static final int OPPO_NETWOKE_TYPE_0 = 0;
    //andriod8.0以上必输
    public static final  String OPPO_CHANNEL_ID = "OPPO PUSH";

    /**
     *vivo推送设置
     */
    //设置通知类型 1：无 2：响铃 3：振动 4：响铃和振动
    public static final int VIVO_NOTIFY_TYPE_2 = 2;
    //消息的生命周期, 若用户离线, 设置消息在服务器保存的时间, 单位: 秒
    public static final int VIVO_TIME_TO_LIVE = 3*24*3600;
    //设置点击跳转类型，value类型支持以下值 1：打开APP首页 2：打开链接  3：自定义  4：打开app内指定页面
    public static final int VIVO_SKIP_TYPE = 2;
    //-1：方式不限 1：仅在wifi下发送 不填默认为-1
    public static final int VIVO_NETWORK_TYPE = -1;
    //根据taskId获得消息统计url
    public static final String VIVO_MSG_STATISTICS_URL = "https://api-push.vivo.com.cn/report/getStatistics";

    /**
     *xiaomi推送设置
     */
    public static final String MI_PACKAGE_NAME = "com.tubatu.demo";
    public static final String MI_HYBRID_PATH = "hybrid_pn";
    //1：使用默认提示音提示 2：使用默认震动提示 4：使用默认led灯光提示 -1（系统默认值）：以上三种效果都有0：以上三种效果都无，即静默推送
    public static final int MI_NOTIFY_TYPE = 1;
    //推送重试次数
    public static final int MI_RETRIES = 0;
    //xiaomi回执类型 1：送达数  2：点击数
    public static final int MI_TYPE_ARRIVED = 1;
    public static final int MI_TYPE_OPEN = 2;
    //xiaomi回调类型 1:消息已送达 2：消息已点击 3：消息送达或点击
    public static final String MI_CALLBACK_TYPE = "3";

    /**
     *huawei推送设置
     */
    //华为获取口令url
    public static final String HUAWEI_ACCESS_TOCKEN_URL = "https://login.cloud.huawei.com/oauth2/v2/token";
    //华为消息推送url
    public static final String HUAWEI_PUSH_URL = "https://api.push.hicloud.com/pushsend.do";
    //华为离线消息过期时间(ms)
    public static final int HUAWEI_EXPIRE_TIME = 3*24*3600*1000;
    //华为推送请求时间戳，必填。(s)
    public static final int HUAWEI_REQEST_TIMESTAMP = 3*60;
    //0：通知栏消息 1：透传消息
    public static final int HUAWEI_PUSH_TYPE = 0;

}
