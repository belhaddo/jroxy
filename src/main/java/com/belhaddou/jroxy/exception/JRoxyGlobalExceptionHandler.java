package com.belhaddou.jroxy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class JRoxyGlobalExceptionHandler {

    @ExceptionHandler(JRoxyIllegalArgumentException.class)
    public ResponseEntity<String> handleJRoxyIllegalArgumentException(JRoxyIllegalArgumentException ex) {
        return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(JRoxyRuntimeException.class)
    public ResponseEntity<String> handleJRoxyRuntimeException(JRoxyRuntimeException ex) {
        return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
