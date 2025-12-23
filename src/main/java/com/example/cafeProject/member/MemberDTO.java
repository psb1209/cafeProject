package com.example.cafeProject.member;

import jakarta.validation.constraints.*;
import com.example.cafeProject.validation.ValidationGroups;
import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO { //일반적인 상황에서 Member 관련 처리를 수행하기 위한 DTO입니다.

    @NotNull(
            message = "대상 회원 id는 필수입니다.",
            groups = ValidationGroups.OnUpdate.class // 수정시 검증
    )
    private Integer id;

    @NotBlank(
            message = "id는 필수입니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    @Size(
            min = 3, max = 20, message = "id는 3~20자 사이여야 합니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "아이디는 영문, 숫자, '_', '-'만 사용할 수 있습니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    ) // 정규식을 통해 유저의 아이디를 조건에 맞는 문자만 사용할 수 있도록 제한합니다.
    private String username;

    @NotBlank(
            message = "password는 필수입니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    @Size(
            min = 8, message = "password는 8자 이상이어야 합니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$",
            message = "password는 영문과 숫자를 각각 최소 1자 이상 포함해야 하며, 허용된 특수문자만 사용할 수 있습니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    ) // 정규식을 통해 유저의 비밀번호를 조건에 맞는 문자만 사용할 수 있도록 제한합니다.
    private String password;

    @NotBlank(
            message = "email은 필수입니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    @Email(
            message = "올바르지 않은 email 형식입니다.",
            groups = ValidationGroups.OnCreate.class // 생성시 검증
    )
    private String email;

    @NotNull(
            message = "변경할 권한은 필수입니다.",
            groups = ValidationGroups.OnUpdate.class // 수정시 검증
    )
    private RoleType role;

    private Timestamp createDate;

    private Grade grade;

    private int postCount;

    private int replyCount;

    public void normalize() { // 필드들의 양 옆 공백을 없애는 메서드
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
        if (email != null) email = email.trim().toLowerCase();
    }
}
