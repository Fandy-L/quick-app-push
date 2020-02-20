package com.chuang.qapp.repository.mysql.push;

import com.chuang.qapp.entity.mysql.push.QappMsgResultKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author fandy.lin
 */
public interface QappMsgResultkeyRepository extends JpaRepository<QappMsgResultKey,Integer> {

    /**
     * 根据provider查询快应用配置信息
     * @param provider
     * @return
     */
    List<QappMsgResultKey> findByProvider(Integer provider);

    /**
     * 根据provider查询快应用配置信息
     * @param time
     * @return
     */
    Optional<QappMsgResultKey> deleteAllByCreateTimeLessThanEqual(Integer time);
}
