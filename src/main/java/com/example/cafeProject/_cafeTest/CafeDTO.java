package com.example.cafeProject._cafeTest;

import com.example.base.BaseDTO;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.validation.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CafeDTO extends BaseDTO {

    @NotBlank(
            message = "카페 이름은 필수입니다.",
            groups = ValidationGroups.OnCreate.class
    )
    @Size(
            max = 100, message = "카페 이름은 100자 이하여야 합니다.",
            groups = ValidationGroups.OnCreate.class
    )
    private String name;

    @Null
    private String nameKey; // 검색용 초성 깬 문자열

    @NotBlank(
            message = "카페 설명은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class}
    )
    private String description;

    private String imgName;

    @NotBlank(
            message = "카페 코드는 필수입니다.",
            groups = ValidationGroups.OnCreate.class
    )
    @Size(
            min = 2, max = 20, message = "카페 코드는 2~20자여야 합니다.",
            groups = ValidationGroups.OnCreate.class
    )
    @Pattern(
            regexp = "^[a-z0-9_-]+$",
            message = "카페 코드는 영문 소문자/숫자/_/-만 사용할 수 있습니다.",
            groups = ValidationGroups.OnCreate.class
    )
    private String code; // 링크에 표시될 코드

    private boolean enabled; // 활성화 여부

    public void normalize() {
        if (name != null) name = name.trim();
        if (code != null) code = code.trim().toLowerCase(Locale.ROOT);
    }
}
