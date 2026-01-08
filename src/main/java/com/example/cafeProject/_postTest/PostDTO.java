package com.example.cafeProject._postTest;

import com.example.base.BaseDTO;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO extends BaseDTO {

    @NotBlank(message = "제목은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private String title;

    @Null
    private String titleKey; // 검색용 초성 깬 문자열

    @NotBlank(message = "내용은 필수입니다.",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private String content;

    @Null
    private Integer cnt;

    @Null
    private Integer likeCnt;

    @Null
    private Boolean likedByMe;

    private boolean notice;

    @Null
    private Boolean deleted;

    @Null
    private Timestamp deletedAt;

    private Integer boardId;

    private String boardName;

    private String boardCode;

    private Grade grade;
}
