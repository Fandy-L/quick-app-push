package com.chuang.qapp.controller;

import com.chuang.qapp.api.QappMsgCallbackApi;
import com.chuang.qapp.entity.RespResult;
import com.chuang.qapp.service.QappMsgCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author fandy.lin
 */
@RestController
public class QuickAppMsgCallbackController implements QappMsgCallbackApi {
    @Autowired
    private QappMsgCallbackService qappMsgCallbackService;

    @Override
    public RespResult xiaomiMsgCallBack(@RequestParam Map<String,String> params) {
        qappMsgCallbackService.dealXiaomiMsgCallBack(params);
        return RespResult.ok();
    }

    @Override
    public RespResult oppoMsgCallBack(@RequestBody List<Map<String,String>> params) {
        qappMsgCallbackService.dealOppoMsgCallBack(params);
        return RespResult.ok();
    }

    @Override
    public RespResult vivoMsgCallBack(@RequestBody String params) {
        qappMsgCallbackService.dealVivoMsgCallBack(params);
        return RespResult.ok();
    }
}
