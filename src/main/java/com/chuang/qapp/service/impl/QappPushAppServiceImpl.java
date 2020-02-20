package com.chuang.qapp.service.impl;

import com.chuang.qapp.entity.mysql.push.QappPushApp;
import com.chuang.qapp.repository.mysql.push.QappPushAppRepository;
import com.chuang.qapp.service.QappPushAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *@author fandy.lin
 */
@Slf4j
@Service
public class QappPushAppServiceImpl implements QappPushAppService {
    @Autowired
    QappPushAppRepository qappPushAppRepository;

    @Override
    public QappPushApp findByProvider(Integer provider){
        return qappPushAppRepository.findByProviderAndEnableAndDeleted(provider,1,0).orElse(null);
    }

}
