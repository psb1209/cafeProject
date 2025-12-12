package com.example.cafeProject.member;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class MemberDeleteDTO {

    // 탈퇴 사유 (선택)
    @Enumerated(EnumType.STRING)
    private ReasonType reason;

    // 현재 비밀번호 (필수)
    @NotBlank(
            message = "password는 필수입니다."
    )
    @Size(
            min = 8, message = "password는 8자 이상이어야 합니다."
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$",
            message = "password는 영문과 숫자를 각각 최소 1자 이상 포함해야 하며, 허용된 특수문자만 사용할 수 있습니다."
    )
    private String password;

    // 동의 체크박스 (필수로 체크되어야 함)
    @AssertTrue(message = "탈퇴 진행을 위해 동의 체크가 필요합니다.")
    private boolean confirm;

    public void normalize() {
        if (password != null) password = password.trim();
    }
}
