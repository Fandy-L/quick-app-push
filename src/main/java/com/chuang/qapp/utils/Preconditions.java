package com.chuang.qapp.utils;

import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.compatible.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author fandy.lin
 */

@Slf4j
public class Preconditions {

    public static String checkStringNotEmpty(String str, Status status){
        if (StringUtils.isEmpty(str)){
            throw new BizException(status);
        }else {
            return str;
        }
    }

    public static String checkStringNotEmpty(String str){
        return checkStringNotEmpty(str, MyExceptionStatus.PARAMS_CONTAINS_NULL);
    }

    public static String checkStringNotBlank(String str,Status status){
        if (StringUtils.isBlank(str)){
            throw new BizException(status);
        }else {
            return str;
        }
    }

    public static Collection checkCollectionSize(Collection collection, Status status, int minSize){
        checkNotNull(collection, MyExceptionStatus.PARAMS_CONTAINS_NULL);
        if (collection.size() < minSize){
            throw new BizException(status);
        }else {
            return collection;
        }
    }

    public static Object checkNotNull(Object o, Status status){
        if (o == null){
            throw new BizException(status);
        }else {
            return o;
        }
    }

    public static Object checkNotNull(Object o, Status status,String message){
        if (o == null){
            log.warn(message);
            throw new BizException(status);
        }else {
            return o;
        }
    }

    public static void checkArgument(boolean expression, MyExceptionStatus status, String errorMessage) {
        if (!expression) {
            if (status != null) {
                throw new BizException(status, errorMessage);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public static void checkArgument(boolean expression, MyExceptionStatus status) {
        if (!expression) {
            if (status != null) {
                throw new BizException(status);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
