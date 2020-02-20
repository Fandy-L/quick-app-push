package com.chuang.qapp.compatible;

import ch.qos.logback.classic.Level;

/**
 * @author fandy.lin
 */
public enum DefaultStatus
        implements Status {
    SUCCESS("success", 200, Level.DEBUG),
    UNAUTHORIZED("permission denied ", 401, Level.WARN),

    FORBIDDEN("forbidden !!!! not permit to call this method!! ", 403, Level.ERROR),

    NO_SUCH_METHOD("no such method", 404),


    INVALID_ARGUMENTS("invalid arguments", 480, Level.WARN),

    UNKNOWN_ERROR("unknown error in server", 500),
    INVALID_REQUEST("invalid request", 501, Level.WARN),
    INVALID_RESPONSE("invalid response", 502, Level.INFO),
    ROUTE_SERVICE_FAIL("route service failed", 503),
    SERVICE_BUSY("service is busy", 504, Level.WARN),
    CALL_SERVICE_TIMEOUT("call service timeout", 505, Level.WARN),

    INTERFACE_NOT_IMPLEMENT("interface not implement", 1001, Level.DEBUG);


    Level lv = Level.ERROR;
    private String msg;

    DefaultStatus(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    private int code;

    DefaultStatus(String msg, int code, Level lv) {
        this.msg = msg;
        this.code = code;
        this.lv = lv;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.msg;
    }

    @Override
    public Level getLogLevel() {
        return this.lv;
    }
}

