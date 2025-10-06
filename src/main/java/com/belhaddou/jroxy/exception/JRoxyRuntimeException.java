package com.belhaddou.jroxy.exception;

public class JRoxyRuntimeException extends RuntimeException {
    public JRoxyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JRoxyRuntimeException(String message) {
        super(message);
    }
}
