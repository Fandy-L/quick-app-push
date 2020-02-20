package com.chuang.qapp.controller;

import com.chuang.qapp.api.QappDeviceUploadApi;
import com.chuang.qapp.entity.wapper.QappDevInfWrapper;
import com.chuang.qapp.entity.RespResult;
import com.chuang.qapp.service.QappDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fandy.lin
 */
@RestController
public class QappDeviceUploadController implements QappDeviceUploadApi {

    @Autowired
    private QappDeviceService qappDeviceService;

    @Override
    public RespResult saveDeviceInfo(@RequestBody  QappDevInfWrapper.DeviceDetailInfDTO reqDTO) {
        qappDeviceService.saveDeviceInfo(reqDTO);
        return RespResult.ok();
    }

}
