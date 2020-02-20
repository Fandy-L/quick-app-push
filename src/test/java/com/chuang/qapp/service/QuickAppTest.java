package com.chuang.qapp.service;

import com.chuang.qapp.BaseApplication;
import com.chuang.qapp.entity.wapper.QappDevInfWrapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class QuickAppTest extends BaseApplication {

    @Autowired
    QappDeviceService quickAppDeviceService;

    /**
     新增单个设备信息
     * */
    @Test
    public void asaveDevInfNew() throws  Exception{
        QappDevInfWrapper.DeviceDetailInfDTO deviceDetailInfDTO = new QappDevInfWrapper.DeviceDetailInfDTO();
        deviceDetailInfDTO.setDeviceId("123456789b2a#1R23");
        deviceDetailInfDTO.setProvider(1);
        deviceDetailInfDTO.setRegId("BXxvqn1nwqCBy99ZKx4Wd9kN3wX3Dw/4D5Ugc0QhE7ZI5Y/fgPq4X7Iu8iWeZPl+++");
        deviceDetailInfDTO.setVersion("7.10.1111");
        quickAppDeviceService.saveDeviceInfo(deviceDetailInfDTO);
    }

    /**
     * 根据regId更新设备
     * */
    @Test
    public void devInfUpdateFromRegId() throws  Exception{
        QappDevInfWrapper.DeviceDetailInfDTO deviceDetailInfDTO = new QappDevInfWrapper.DeviceDetailInfDTO();
        deviceDetailInfDTO.setDeviceId("123456789b2a#122");
        deviceDetailInfDTO.setProvider(1);
        deviceDetailInfDTO.setRegId("BXxvqn1nwqCBy99ZKx4Wd9kN3wX3Dw/4D5Ugc0QhE7ZI5Y/fgPq4X7Iu8iWeZPl+++");
        deviceDetailInfDTO.setVersion("7.10.12");
        quickAppDeviceService.saveDeviceInfo(deviceDetailInfDTO);
    }

    /**
     * 根据deviceId更新设备
     * */
    @Test
    public void devInfUpdateFromDeviceId() throws  Exception{

        QappDevInfWrapper.DeviceDetailInfDTO deviceDetailInfDTO = new QappDevInfWrapper.DeviceDetailInfDTO();
        deviceDetailInfDTO.setDeviceId("123456789b2a#122");
        deviceDetailInfDTO.setProvider(1);
        deviceDetailInfDTO.setRegId("BXxvqn1nwqCBy99ZKx4Wd9kN3wX3Dw/4D5Ugc0QhE7ZI5Y/fgPq4X7Iu8iWeZPl+");
        deviceDetailInfDTO.setVersion("7.10.13");
        quickAppDeviceService.saveDeviceInfo(deviceDetailInfDTO);
    }



    /**
     * 一个月定期删除信息
     */
    @Test
    public void infoClear(){
        quickAppDeviceService.removeDevInfOfMonth();
    }




}
