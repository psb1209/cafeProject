package com.example.cafeProject._postTest;

import com.example.cafeProject._boardTest.Board;
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
public class PostDTO {

    private String title;

    private String content;

    private Integer cnt;

    private boolean notice;

    private String boardCode;

    private String boardName;
}
