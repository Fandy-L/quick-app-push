package com.chuang.qapp.repository.mysql.push;

import com.chuang.qapp.entity.mysql.push.QappDeviceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author fandy.lin
 */
public interface QappDeviceInfoRepository extends JpaRepository<QappDeviceInfo,Integer> {

    /**
     * 根据deviceid获取整个设备信息。
     * @param deviceId
     * @return
     */

    Optional<QappDeviceInfo> findByDeviceId(String deviceId);

    /**
     * 根据regId获取整个设备信息。
     * @param deviceId
     * @param provider
     * @return
     */
    Optional<QappDeviceInfo> findByRegIdAndProvider(String deviceId, Integer provider);


    /**
     * 删除一个月未进行设备信息更新的设备信息
     * @param timeNode
     */
    void deleteAllByUpdateTimeLessThanEqual(int timeNode);

    /**
     *某个厂商下的设备总数
     * @param provider
     * @return
     */
    Long countByProvider(Integer provider);

    /**
     * 某个厂商下的设备信息分页查询
     * @param provider
     * @param pageable
     * @return
     */
    Page<QappDeviceInfo> findAllByProvider(Integer provider, Pageable pageable);

    /**
     * 根据regid批量查询设备信息
     * @param regIds
     * @return
     */
    List<QappDeviceInfo> findByDeviceIdIn(List<String> regIds);

    /**
     * 根据provider和regIds查询设备信息
     *
     * @param provider
     * @param deviceIds
     * @return
     */
    @Query(value = "select a.regId from QappDeviceInfo a where a.provider=:provider and a.deviceId in :deviceIds")
    List<String> findByProviderAndDeviceIdIn(@Param("provider") Integer provider, @Param("deviceIds") List<String> deviceIds);

    @Query(value = "select a.regId from QappDeviceInfo  a where a.provider=:provider and a.deviceId=:deviceId")
    List<String> findByProviderAndDeviceId(@Param("provider") Integer provider, @Param("deviceId") String deviceId);
}
