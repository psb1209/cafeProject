package com.example.exception;

import lombok.Getter;

@Getter
public class DuplicateValueException extends RuntimeException {
    private final String field;   // "name" or "code"
    private final String value;

    // 메시지만 받는 생성자
    public DuplicateValueException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    // 메시지 + 원인(Exception)까지 같이 넘기고 싶을 때
    public DuplicateValueException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }

    // 필드와 값을 같이 받는 생성자
    public DuplicateValueException(String message, String field, String value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    // 필드 + 값 + 메세지 + 원인
    private DuplicateValueException(String message, String field, String value, Throwable cause) {
        super(message, cause);
        this.field = field;
        this.value = value;
    }
}

