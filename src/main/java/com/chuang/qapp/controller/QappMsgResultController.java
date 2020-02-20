package com.chuang.qapp.controller;

import com.chuang.qapp.api.QappMsgResultApi;
import com.chuang.qapp.entity.RespResult;
import com.chuang.qapp.entity.vo.MsgResultVO;
import com.chuang.qapp.entity.wapper.QuickAppMsgResultWrapper;
import com.chuang.qapp.service.QappMsgResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author fandy.lin
 */
@RestController
public class QappMsgResultController implements QappMsgResultApi {
    @Autowired
    private QappMsgResultService qappMsgResultService;

    @Override
    public RespResult openResult(@RequestBody QuickAppMsgResultWrapper.MsgOpenResultDTO reqDTO) {
        qappMsgResultService.saveOpenResult(reqDTO.getMsgOpenResult());
        return RespResult.ok();
    }

    @Override
    public RespResult<List<MsgResultVO>> findMsgResult(@RequestBody List<Integer> bizMsgId) {
        return RespResult.data(qappMsgResultService.findQuickAppMsgResult(bizMsgId));
    }
}
