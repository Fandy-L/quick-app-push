package com.chuang.qapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chuang.qapp.common.Constants;
import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.common.QappMsgConstant;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.dto.MsgOpenResultDTO;
import com.chuang.qapp.entity.dto.MsgResultDTO;
import com.chuang.qapp.entity.mysql.push.QappMsgResult;
import com.chuang.qapp.entity.mysql.push.QappMsgResultKey;
import com.chuang.qapp.entity.vo.MsgResultVO;
import com.chuang.qapp.repository.mysql.push.QappMsgResultRepository;
import com.chuang.qapp.repository.mysql.push.QappMsgResultkeyRepository;
import com.chuang.qapp.service.AbstractQuickAppMsgService;
import com.chuang.qapp.service.QappMsgResultService;
import com.chuang.qapp.service.push.QappPushSetter;
import com.chuang.qapp.utils.DozerUtils;
import com.chuang.qapp.utils.HttpUtils;
import com.chuang.qapp.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author fandy.lin
 */
@Slf4j
@Service
public class QappMsgResultServiceImpl extends AbstractQuickAppMsgService implements QappMsgResultService {
    @Autowired
    private QappMsgResultRepository qappMsgResultRepository;
    @Autowired
    private QappMsgResultkeyRepository qappMsgResultkeyRepository;
    @Resource
    private QappPushSetter vivoQappPushProvider;
    @Autowired
    private HttpUtils httpUtils;


    @Override
    public void saveOpenResult(MsgOpenResultDTO reqDTO) {
        String cacheKey = MessageFormat.format("{0}_{1}_{2}", reqDTO.getMsgId(), reqDTO.getProvider(), QappMsgConstant.OPEN_SUFFIX);
        this.addToHyperLogLog(cacheKey, new String[]{reqDTO.getDeviceId()});
    }

    @Override
    public void doMessageStatistics() {
        //只进行vivo的主动消息统计
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/application/json");
        header.put("authToken", vivoQappPushProvider.getToken());
        //获得taskId的统计
        List<QappMsgResultKey> msgResultKeys = qappMsgResultkeyRepository.findByProvider(QappMsgConstant.PROVIDER_VIVO);
        for (QappMsgResultKey msgResultKey : msgResultKeys) {
            if (msgResultKey.getProviderMsgId() != null) {
                String url = MessageFormat.format("{0}?taskIds={1}", QappMsgConstant.VIVO_MSG_STATISTICS_URL, msgResultKey.getProviderMsgId());
                String resultStr = null;
                try {
                    resultStr = httpUtils.doGet(url,header);
                } catch (IOException e) {
                    log.warn("vivo快应用消息统计响应报文网络异常:", e);
                    throw new BizException(MyExceptionStatus.QUICK_APP_VIVO_STATISTICS_LINK_ERR,e);
                }
                log.info("vivo消息统计响应报文:{}", resultStr);
                JSONObject jsonResult = JSON.parseObject(resultStr);
                JSONArray statistics = jsonResult.getJSONArray("statistics");
                JSONObject data = statistics.getJSONObject(0);
                String msgId = msgResultKey.getMsgKey().split("_")[0];
                QappMsgResult msgResult = qappMsgResultRepository.findByMsgIdAndProvider(msgId, QappMsgConstant.PROVIDER_VIVO).orElse(null);
                if (msgResult == null) {
                    log.error("vivo进行主动消息统计返回报文:{} ,对应msgId不存在:{}", resultStr, msgId);
                    throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_VIVO_MSG_ID_MISS);
                }
                //更新消息结果
                msgResult.setArrivedNum(data.getInteger("receive"));
                msgResult.setProviderOpenNum(data.getInteger("click"));
                this.updateMsgResult(msgResult);
            }
        }
    }

    @Override
    public List<QappMsgResult> findQuickAppMsgResult(Integer bizMsgId) {
        return qappMsgResultRepository.findByBizMsgId(bizMsgId);
    }

    @Override
    public List<MsgResultVO> findQuickAppMsgResult(List<Integer> bizMsgId) {
        return qappMsgResultRepository.findAllGroupByBizMsgIds(bizMsgId);
    }

    @Override
    public void saveOrIncreaseMsgResultNum(MsgResultDTO msgResultDTO) {
        QappMsgResult result = qappMsgResultRepository.findByMsgIdAndProvider(msgResultDTO.getMsgId(), msgResultDTO.getProvider()).orElse(null);
        if (result != null) {
            result.setPushNum(result.getPushNum() + msgResultDTO.getPushNum());
            result.setArrivedNum(result.getArrivedNum() + msgResultDTO.getArrivedNum());
            result.setOpenNum(result.getOpenNum() + msgResultDTO.getOpenNum());
            result.setProviderOpenNum(result.getProviderOpenNum() + msgResultDTO.getProviderOpenNum());
        } else {
            result = DozerUtils.map(msgResultDTO, QappMsgResult.class);
            result.setCreateTime(TimeUtils.getCurrentTimestamp());
            result.setDeleted(Constants.NOT_BE_DELETED);
        }
        this.updateMsgResult(result);
    }

    @Override
    public void updateMsgResult() {
        //对过期消息进行清除
        qappMsgResultkeyRepository.deleteAllByCreateTimeLessThanEqual(TimeUtils.getCurrentTimestamp() - QappMsgConstant.PUSH_RESULT_EXPIRE_TIME);
        //更新消息
        List<QappMsgResultKey> resultKeys = qappMsgResultkeyRepository.findAll();
        String msgKey;
        String openKey;
        String providerOpenKey;
        int providerOpenNum;
        int openNum;
        int arrivedNum;
        String arrivedKey;
        String[] split;
        String msgId;
        Integer provider;
        QappMsgResult result;
        for (QappMsgResultKey resultKey : resultKeys) {
            msgKey = resultKey.getMsgKey();
            split = msgKey.split("_");
            msgId = split[0];
            provider = Integer.parseInt(split[0]);
            result = qappMsgResultRepository.findByMsgIdAndProvider(msgId, provider).orElse(null);
            if (result == null) {
                log.error("快应用消息统计对应msgId不存在:{}",msgId);
                throw new BizException(MyExceptionStatus.QUICK_APP_PUSH_RESULT_MSG_ID_MISS);
            }
            if (provider == QappMsgConstant.PROVIDER_XIAOMI) {
                providerOpenKey = MessageFormat.format("{0}_{1}", msgKey, QappMsgConstant.PROVIDER_OPEN_SUFFIX);
                providerOpenNum = (int) this.getSetSize(providerOpenKey);
                result.setProviderOpenNum(providerOpenNum);
            }
            openKey = MessageFormat.format("{0}_{1}", msgKey, QappMsgConstant.OPEN_SUFFIX);
            openNum = (int) this.getSetSize(openKey);
            arrivedKey = MessageFormat.format("{0}_{1}", msgKey, QappMsgConstant.ARRIVED_SUFFIX);
            arrivedNum = (int) this.getSetSize(arrivedKey);
            result.setOpenNum(openNum);
            result.setArrivedNum(arrivedNum);
            this.updateMsgResult(result);
        }
    }

    private void updateMsgResult(QappMsgResult qappMsgResult) {
        qappMsgResult.setUpdateTime(TimeUtils.getCurrentTimestamp());
        qappMsgResultRepository.save(qappMsgResult);
    }

}

