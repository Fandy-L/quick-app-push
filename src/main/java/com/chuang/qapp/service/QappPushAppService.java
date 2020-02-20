package com.chuang.qapp.service;


import com.chuang.qapp.entity.mysql.push.QappPushApp;

/**
 * @author fandy.lin
 */
public interface QappPushAppService {

    /**
     * 根据厂商编号查询快应用配置信息
     * @param provider
     * @return
     */
    QappPushApp findByProvider(Integer provider);

}
