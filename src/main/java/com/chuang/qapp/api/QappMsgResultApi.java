package com.chuang.qapp.api;

import com.chuang.qapp.entity.RespResult;
import com.chuang.qapp.entity.vo.MsgResultVO;
import com.chuang.qapp.entity.wapper.QuickAppMsgResultWrapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author fandy.lin
 */

@Validated
public interface QappMsgResultApi {

    /**
     * 前端用户点击消息反馈
     * @param
     * @return
     */
    @PostMapping(value = "/qapp/msg/result/open")
    RespResult openResult(@Valid QuickAppMsgResultWrapper.MsgOpenResultDTO reqDTO);

    /**
     * 管理后台系统查询统计数据
     * @param
     * @return
     */
    @PostMapping(value = "/qapp/msg/result")
    RespResult<List<MsgResultVO>> findMsgResult(@Valid @NotEmpty List<@NotNull Integer> bizMsgId);

}
