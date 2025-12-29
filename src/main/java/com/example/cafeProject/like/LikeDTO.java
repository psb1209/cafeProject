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
}
