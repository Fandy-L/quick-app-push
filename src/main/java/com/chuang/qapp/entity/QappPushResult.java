package com.chuang.qapp.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 快应用返回结果封装
 * @author fandy.lin
 * 2019.11.26
 */
@Data
@Builder
@Accessors(chain = true)
public class QappPushResult {
    private Integer successTotal;
    private Integer failTotal;
}
