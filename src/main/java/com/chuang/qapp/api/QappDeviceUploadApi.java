package com.chuang.qapp.api;

import com.chuang.qapp.entity.wapper.QappDevInfWrapper;
import com.chuang.qapp.entity.RespResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author fandy.lin
 * 快应用push设备信息上报api
 */

@Validated
public interface QappDeviceUploadApi {

    /**
     * 存储设备信息
     * @param reqDTO
     * @return
     */
    @PostMapping(value = "/qapp/dev/save")
    RespResult saveDeviceInfo(@RequestBody QappDevInfWrapper.DeviceDetailInfDTO reqDTO);
}
