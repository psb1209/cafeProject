package com.example.cafeProject.board_view;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Board_viewDTO {

    private int id;

    private int UserId;

    private int likeCnt;

    private Integer communityBoardNumber;

    private Integer operationBoardNumber;

    private Integer noticeBoardNumber;

    private Integer informationBoardNumber;
}
