package com.example.cafeProject.like;


import lombok.*;

@Getter
@Setter
public class LikeDTO {

    private int id;

    private int UserId;

    private int likeCnt;

    private Integer communityBoardNumber;

    private Integer operationBoardNumber;

    private Integer noticeBoardNumber;

    private Integer informationBoardNumber;

    private Integer postId;

    // 리다이렉트 URL 만들기용
    private String cafeCode;
    private String boardCode;
}
