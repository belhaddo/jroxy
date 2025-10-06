package com.belhaddou.jroxy.exception;

public class JRoxyIllegalArgumentException extends IllegalArgumentException {
    public JRoxyIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public JRoxyIllegalArgumentException(String message) {
        super(message);
    }
}
