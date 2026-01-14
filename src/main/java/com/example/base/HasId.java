package com.example.base;

/**
 * BaseEntity/BaseDTO를 상속하지 않는 경우를 위한 최소 계약 인터페이스.
 * - getId()만 있으면 Base 계열에서 자동으로 id를 추출할 수 있습니다.
 */
public interface HasId<ID> {
    ID getId();
}

