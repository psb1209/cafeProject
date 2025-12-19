package com.example.cafeProject._postTest;

import com.example.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO extends BaseDTO {

    private String title;

    private String titleKey; // 검색용 초성 깬 문자열

    private String content;

    private Integer cnt;

    private boolean notice;

    private Integer boardId;

    private String boardName;

    private String boardCode;
}
