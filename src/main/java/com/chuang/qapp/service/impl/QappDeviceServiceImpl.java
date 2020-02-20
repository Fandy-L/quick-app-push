package com.chuang.qapp.service.impl;

import com.chuang.qapp.common.Constants;
import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.mysql.push.QappDeviceInfo;
import com.chuang.qapp.entity.wapper.QappDevInfWrapper;
import com.chuang.qapp.repository.mysql.push.QappDeviceInfoRepository;
import com.chuang.qapp.service.QappDeviceService;
import com.chuang.qapp.utils.DozerUtils;
import com.chuang.qapp.utils.TimeUtils;
import com.chuang.qapp.utils.ValidUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author fandy.lin
 */
@Slf4j
@Service
@Transactional(transactionManager = "pushTransactionManager",rollbackFor = Exception.class)
public class QappDeviceServiceImpl implements QappDeviceService {
    @Value("${qapp.device.invalid.regids:test_id,regid-abcdefg}")
    private String[] invalidRegIds;

    @Autowired
    private QappDeviceInfoRepository deviceInfoRepository;

    @Override
    public void saveDeviceInfo(QappDevInfWrapper.DeviceDetailInfDTO reqDTO) {
        ValidUtils.valid(reqDTO);
        this.filterInvalidRegids(reqDTO.getRegId());
        QappDeviceInfo deviceInfoForSave = DozerUtils.map(reqDTO, QappDeviceInfo.class);
        QappDeviceInfo deviceInfoForId = deviceInfoRepository.findByDeviceId(reqDTO.getDeviceId()).orElse(null);
        //新增设备信息
        if(deviceInfoForId == null){
            //插入数据之前，根据regId查一次，存在记录则更新regId对应记录
            QappDeviceInfo regDevInf = deviceInfoRepository.findByRegIdAndProvider(reqDTO.getRegId(),reqDTO.getProvider()).orElse(null);
            if(regDevInf != null){
                this.updateDevInfFromMap(regDevInf,deviceInfoForSave);
            }else{
                //否则新增一条记录
                saveDevInf(deviceInfoForSave);
            }
            //更新设备信息
        }else{
            this.updateDevInfFromMap(deviceInfoForId,deviceInfoForSave);
        }
    }

    private void filterInvalidRegids(String regId) {
        for(String invalidRegId: invalidRegIds){
            if (invalidRegId.contains(regId)){
                throw  new BizException(MyExceptionStatus.QUICK_APP_DEVICE_REGIDS_INVALID);
            }
        }
    }

    @Override
    public void removeDevInfOfMonth() {
        deviceInfoRepository.deleteAllByUpdateTimeLessThanEqual(TimeUtils.getCurrMonthAgoTime());
    }

    /**
     * 由旧记录关键信息到新记录的映射后更新设备信息
     */
    private void updateDevInfFromMap(QappDeviceInfo oldInf,QappDeviceInfo newInf){
        newInf.setId(oldInf.getId());
        newInf.setCreateTime(oldInf.getCreateTime());
        newInf.setDeleted(oldInf.getDeleted());
        this.updateDevInf(newInf);
    }

    private void updateDevInf(QappDeviceInfo entity) {
        entity.setUpdateTime(TimeUtils.getCurrentTimestamp());
        deviceInfoRepository.save(entity);
    }

    private void saveDevInf(QappDeviceInfo entity) {
        try {
            entity.setCreateTime(TimeUtils.getCurrentTimestamp());
            entity.setDeleted(Constants.NOT_BE_DELETED);
            updateDevInf(entity);
        }catch (ConstraintViolationException e){
            log.warn("出现相同的设备信息重复保存:{}", entity.toString(),e);
        }
    }

    @Override
    public List<QappDeviceInfo> findAllDevInfByProvider(Integer provider, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return deviceInfoRepository.findAllByProvider(provider, pageable).getContent();
    }

    @Override
    public Integer countByProvider(Integer provider) {
        return deviceInfoRepository.countByProvider(provider).intValue();
    }

    @Override
    public List<QappDeviceInfo> findByDeviceIds(List<String> regIds) {
        return deviceInfoRepository.findByDeviceIdIn(regIds);
    }

    @Override
    public List<String> findRegIdsByProviderAndDeviceId(Integer provider, List<String> deviceIds){
        return deviceInfoRepository.findByProviderAndDeviceIdIn(provider,deviceIds);
    }

    @Override
    public String findRegIdByProviderAndDeviceId(Integer provider, String deviceId) {

        List<String> regIds = deviceInfoRepository.findByProviderAndDeviceId(provider,deviceId);
        return regIds.size() > 0 ? regIds.get(0) : null;
    }
}
