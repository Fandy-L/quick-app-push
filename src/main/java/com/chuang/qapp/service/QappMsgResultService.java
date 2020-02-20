package com.chuang.qapp.service;

import com.chuang.qapp.entity.dto.MsgOpenResultDTO;
import com.chuang.qapp.entity.dto.MsgResultDTO;
import com.chuang.qapp.entity.mysql.push.QappMsgResult;
import com.chuang.qapp.entity.vo.MsgResultVO;

import java.util.List;

public interface QappMsgResultService {
    /**
     * 保存打开数
     * @param reqDTO
     */
    void saveOpenResult(MsgOpenResultDTO reqDTO);

    /**
     * 调用厂商接口主动进行消息统计
     */
    void doMessageStatistics();


    /**
     * 根据php业务msgid获取统计结果
     * @param bizMsgId
     * @return
     */
    List<MsgResultVO> findQuickAppMsgResult(List<Integer> bizMsgId);

    /**
     * 保存或累增QuickAppMsgResult计数值
     * @param msgResultDTO
     * @return
     */
    void saveOrIncreaseMsgResultNum(MsgResultDTO msgResultDTO);

    /**
     *从缓存更新QuickAppMsgResult计数值
     */
    void updateMsgResult();

    /**
     * 根据bizMsgId查询列表
     * @param bizMsgId
     * @return
     */
    List<QappMsgResult> findQuickAppMsgResult(Integer bizMsgId);


}
