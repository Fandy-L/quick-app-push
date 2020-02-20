package com.chuang.qapp.repository.mysql.push;

import com.chuang.qapp.entity.mysql.push.QappPushApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author fandy.lin
 */
public interface QappPushAppRepository extends JpaRepository<QappPushApp,Integer> {

    /**
     * 根据provider查询快应用配置信息
     * @param provider
     * @param enable
     * @param deleted
     * @return
     */
    Optional<QappPushApp> findByProviderAndEnableAndDeleted(Integer provider, Integer enable, Integer deleted);

}
