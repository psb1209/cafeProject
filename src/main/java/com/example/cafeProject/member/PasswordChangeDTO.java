package com.example.cafeProject.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO { // 비밀번호 변경을 검증하기 위한 DTO입니다.

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "password는 8자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$",
            message = "password는 영문과 숫자를 각각 최소 1자 이상 포함해야 하며, 허용된 특수문자만 사용할 수 있습니다."
    ) // 정규식을 통해 유저의 비밀번호를 조건에 맞는 문자만 사용할 수 있도록 제한합니다.
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;

    public void normalize() { // 필드들의 양 옆 공백을 없애는 메서드
        if (currentPassword != null) currentPassword = currentPassword.trim();
        if (newPassword != null) newPassword = newPassword.trim();
        if (confirmPassword != null) confirmPassword = confirmPassword.trim();
    }
}

