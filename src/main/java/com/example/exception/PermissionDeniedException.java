package com.example.exception;

public class PermissionDeniedException extends RuntimeException {

    // 메시지만 받는 생성자
    public PermissionDeniedException(String message) {
        super(message);
    }

    // 메시지 + 원인(Exception)까지 같이 넘기고 싶을 때
    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}

