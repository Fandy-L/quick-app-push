package com.chuang.qapp.service;

import java.util.List;
import java.util.Map;

/**
 * @author fandy.lin
 */
public interface QappMsgCallbackService {

    /**
     * 处理小米回执
     * @param params
     * @return
     */
    void dealXiaomiMsgCallBack(Map<String, String> params);

    /**
     * 处理oppo回执
     * @param params
     * @return
     */
    void dealOppoMsgCallBack(List<Map<String, String>> params);

    /**
     * 处理vivo回执
     * @param params
     * @return
     */
    void dealVivoMsgCallBack(String params);

}
