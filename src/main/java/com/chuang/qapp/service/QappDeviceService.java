package com.chuang.qapp.service;

import com.chuang.qapp.entity.mysql.push.QappDeviceInfo;
import com.chuang.qapp.entity.wapper.QappDevInfWrapper;

import java.util.List;

/**
 * @author fandy.lin
 */
public interface QappDeviceService {

    /**
     * 存储设备信息
     * @param reqDTO
     * @return
     */
    void saveDeviceInfo(QappDevInfWrapper.DeviceDetailInfDTO reqDTO);

    /**
     * 物理删除一个月未更新的设备信息
     */
    void removeDevInfOfMonth();

    /**
     * 通过provider查询指定页数与数量的厂商设备信息
     * @param provider
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<QappDeviceInfo> findAllDevInfByProvider(Integer provider, Integer pageNum, Integer pageSize);
    /**
     * 计算当前厂商下的设备信息总数
     * @param provider
     * @return
     */
    Integer countByProvider(Integer provider);

    /**
     * 根据deviceId查询设备信息
     * @param deviceIds
     * @return
     */
    List<QappDeviceInfo> findByDeviceIds(List<String> deviceIds);

    /**
     * 根据provider和deviceId查询设备信息，返回regId集合
     *
     * @param provider
     * @param deviceIds
     * @return
     */
    List<String> findRegIdsByProviderAndDeviceId(Integer provider, List<String> deviceIds);

    /**
     * 根据provider和deviceId查找regid
     *
     * @param provider
     * @param deviceId
     * @return
     */
    String findRegIdByProviderAndDeviceId(Integer provider, String deviceId);
}
