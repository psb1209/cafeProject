package com.example.cafeProject._boardTest;

import com.example.base.BaseDTO;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO extends BaseDTO {

    @NotBlank(
            message = "게시판 이름은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    @Size(
            max = 100, message = "게시판 이름은 100자 이하여야 합니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    private String name;

    @NotBlank(
            message = "게시판 설명은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    private String description;

    private String imgName;

    @NotBlank(
            message = "게시판 코드는 필수입니다.",
            groups = ValidationGroups.OnCreate.class
    )
    @Size(
            min = 2, max = 20, message = "게시판 코드는 2~20자여야 합니다.",
            groups = ValidationGroups.OnCreate.class
    )
    @Pattern(
            regexp = "^[a-z0-9_-]+$",
            message = "게시판 코드는 영문 소문자/숫자/_/-만 사용할 수 있습니다.",
            groups = ValidationGroups.OnCreate.class
    )
    private String code; // 링크에 표시될 코드

    private boolean enabled; // 활성화 여부

    @NotNull(
            message = "작성 권한은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    private RoleType writeRole; // 작성 권한

    @NotNull(
            message = "보기 권한은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    private RoleType readRole; // 보기 권한
}
