package com.chuang.qapp.service;

import com.chuang.qapp.common.QappMsgConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractQuickAppMsgService {
    @Autowired
    protected RedisTemplate<String,String> redisTemplate;
    @Value("${qapp.push.result.open.num.limit:50000}")
    private int openNumLimit;

    /**
     * 存储到redis hyperloglog数据统计结构中
     * @param key
     * @param values
     */
    protected void addToHyperLogLog(String key,String[] values){
        HyperLogLogOperations<String, String> operations = redisTemplate.opsForHyperLogLog();
        if(redisTemplate.hasKey(key)){
            operations.add(key,values);
        }else{
            operations.add(key,values);
            redisTemplate.expire(key, QappMsgConstant.PUSH_RESULT_EXPIRE_TIME, TimeUnit.SECONDS);
        }
        //获得到达数统计
        long hyperLogLogSize = getHyperLogLogSize(key);
        log.info("厂商key:{} ,hyperLogLog到达数统计计数大小:{}",key,hyperLogLogSize);
    }

    /**
     * 存储到redis set集合
     * @param key
     * @param values
     */
    protected void addToSet(String key,String[] values){
        SetOperations<String, String> operations = redisTemplate.opsForSet();
        if(redisTemplate.hasKey(key)){
            if(operations.size(key) < openNumLimit){
                operations.add(key,values);
            }else{
                log.warn("厂商 key为：{}，超出限制最大点击量:{} ，请排查！",key,openNumLimit);
            }
        }else{
            operations.add(key,values);
            redisTemplate.expire(key, QappMsgConstant.PUSH_RESULT_EXPIRE_TIME, TimeUnit.SECONDS);
        }
    }

    /**
     * 从redis hyperloglog获得数据统计
     * @param key
     */
    protected long getHyperLogLogSize(String key){
        HyperLogLogOperations<String, String> operations = redisTemplate.opsForHyperLogLog();
        Long size = operations.size(key);
        return size==null?0:size.longValue();
    }

    /**
     * 从redis set获得数据统计
     * @param key
     */
    protected long getSetSize(String key){
        SetOperations<String, String> operations = redisTemplate.opsForSet();
        Long size = operations.size(key);
        return size==null?0:size.longValue();
    }
}
