package edu.cdut.aiback.common;

/**
 * 业务异常
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}
