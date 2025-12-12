package com.example.exception;

public class EntityNotFoundException extends RuntimeException {

    // 메시지만 받는 생성자
    public EntityNotFoundException(String message) {
        super(message);
    }

    // 메시지 + 원인(Exception)까지 같이 넘기고 싶을 때
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

