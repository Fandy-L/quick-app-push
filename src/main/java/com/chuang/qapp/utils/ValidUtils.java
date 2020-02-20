package com.chuang.qapp.utils;

import com.chuang.qapp.common.MyExceptionStatus;
import com.chuang.qapp.compatible.BizException;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author fandy.lin
 */
public class ValidUtils {
    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    public static void valid(Object o) {
        if (o == null){
            throw new BizException(MyExceptionStatus.PARAMS_CONTAINS_NULL);
        }
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(o);
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
            sb.append(constraintViolation.getPropertyPath());
            sb.append(constraintViolation.getMessage());
            sb.append(";");
        }
        if (StringUtils.isEmpty(sb)){
            throw new BizException(sb.toString());
        }
}}
