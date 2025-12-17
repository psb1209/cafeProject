package com.example.cafeProject.boardTest;

import com.example.base.BaseDTO;
import com.example.cafeProject.member.RoleType;
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

    @NotBlank(message = "게시판 이름은 필수입니다.")
    @Size(max = 100, message = "게시판 이름은 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "게시판 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "게시판 코드는 필수입니다.")
    @Size(min = 2, max = 20, message = "게시판 코드는 2~20자여야 합니다.")
    @Pattern(
            regexp = "^[a-z0-9_-]+$",
            message = "게시판 코드는 영문 소문자/숫자/_/-만 사용할 수 있습니다."
    )
    private String code; // 링크에 표시될 코드

    private boolean enabled; // 활성화 여부

    @NotNull(message = "작성 권한은 필수입니다.")
    private RoleType writeRole; // 작성 권한
}
