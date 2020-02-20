package com.chuang.qapp.exception;

import com.chuang.qapp.compatible.BizException;
import com.chuang.qapp.entity.RespResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ControllerAdvice

@ResponseBody
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Set<Class<?>> DEFAULT_WARN_EXCEPTION = Stream.of(MethodArgumentNotValidException.class, NoHandlerFoundException.class, MissingServletRequestParameterException.class, UnsatisfiedServletRequestParameterException.class, HttpMediaTypeException.class, HttpMediaTypeNotAcceptableException.class, HttpMediaTypeNotSupportedException.class).collect(Collectors.toSet());

    private final Set<Class<?>> warnException;

    public GlobalExceptionHandler(@Value("${spring.include.exception:false}") String exclude) {
        this.warnException = new HashSet<>();
        this.warnException.addAll(DEFAULT_WARN_EXCEPTION);
        if ("false".equals(exclude)) {
            return;
        }
        assert StringUtils.split(exclude, ",") != null;
        Set<Class<?>> cusClz =  Stream.of(StringUtils.split(exclude, ",")).map
                (String::trim).map(r -> {
                    try {
                        return Class.forName(r);
                    } catch (Exception e) {
                        log.warn("", e);
                        return null;
                    }
                }).collect(Collectors.toSet());
        this.warnException.addAll(cusClz);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.OK)
    public RespResult handle(BizException exception) {
        String first = exception.getStatus().getMessage();
        String second = exception.getMessage();
        String composed = first;
        if (first != null && !first.isEmpty()) {
            composed = composed + ((second == null || second.isEmpty()) ? "" : ("|||" + second));
        } else {
            composed = second;
        }
        if (exception.getResult() != null) {
            composed = composed + "@" + exception.getResult();
        }
        log.warn("子业务抛出biz异常,内容为：{}", composed, exception);
        return RespResult.fail(exception.getStatus().getCode(), composed);
    }

   @ExceptionHandler
    @ResponseStatus(HttpStatus.OK)
    public RespResult handle(Exception exception) {
        return error(exception, exception.getMessage());
    }

    private RespResult error(Exception e, String message) {
        if (this.warnException.contains(e.getClass())) {
            log.warn("参数校验异常,错误信息:{},堆栈:", message, e);

        } else {
            log.error("业务异常,错误信息:{},堆栈:", message, e);
        }
        return RespResult.fail(message);
    }
}



