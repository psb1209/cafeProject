package com.example.cafeProject.informationBoardComment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class InformationBoardCommentDTO {


    private int id;
    private String content;
    private Timestamp createDate;
    private int memberId;
    private int informationBoardId;
    private int informationBoardCommentId;

    /*============================================== 대댓글 ===============================================*/
    private int ref;
    private int step;
    private int level;
    /*============================================== 대댓글 ===============================================*/


}
