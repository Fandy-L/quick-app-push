package com.chuang.qapp.repository.mysql.push;

import com.chuang.qapp.entity.mysql.push.QappMsgResult;
import com.chuang.qapp.entity.vo.MsgResultVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author fandy.lin
 */
public interface QappMsgResultRepository extends JpaRepository<QappMsgResult,Integer> {

    /**
     * 根据bizMsgId查询快应用配置信息
     * @return
     */
    List<QappMsgResult> findByBizMsgId(Integer bizMsgId);

    /**
     * 根据bizMsgId查询推送结果信息
     * @return
     */
    List<QappMsgResult> findAllByBizMsgId(Integer bizMsgId);

    /**
     * 根据msgId与provider查询快应用配置信息
     * @return
     */
    Optional<QappMsgResult> findByMsgIdAndProvider(String msgId, Integer provider);


    /**
     * 根据bizMsgId与provider查询一个厂商
     * @return
     */
    Optional<QappMsgResult> findByBizMsgIdAndProvider(Integer bizMsgId, Integer provider);


    /**
     * 根据bizMsgId与provider查询快应用配置信息
     * @return
     */
    @Query("select new com.chuang.qapp.entity.vo.MsgResultVO(r.bizMsgId,sum(r.pushNum),sum(r.arrivedNum),sum(r.openNum))from QappMsgResult r where  r.bizMsgId in(:bizMsgIds) group by r.bizMsgId")
    List<MsgResultVO> findAllGroupByBizMsgIds(@Param("bizMsgIds") List<Integer> bizMsgIds);

}
