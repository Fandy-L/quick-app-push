package com.chuang.qapp.compatible;

import ch.qos.logback.classic.Level;

/**
 * @author fandy.lin
 */
public interface Status {
    int getCode();

    String getMessage();

    Level getLogLevel();

    default String toStr() {
        return "message:[" + getMessage() + "] status=[" + getCode() + "]";
    }
}
