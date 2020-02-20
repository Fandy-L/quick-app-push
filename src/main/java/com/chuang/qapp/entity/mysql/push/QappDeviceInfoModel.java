package com.chuang.qapp.entity.mysql.push;

import lombok.Data;

/**
 * @author fandy.lin
 */
@Data
public class QappDeviceInfoModel {

    private String regId;

    private Integer provider;

    public QappDeviceInfoModel(String regId, Integer provider){
        this.regId = regId;
        this.provider = provider;
    }
}
