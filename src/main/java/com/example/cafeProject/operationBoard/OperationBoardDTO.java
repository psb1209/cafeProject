package com.example.cafeProject.operationBoard;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class OperationBoardDTO {

    private int id;

    private String subject;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int cnt;

    /*============================================== 대댓글 ===============================================*/
    private int operationBoardCommentId;

    private String operationBoardCommentContent;
    /*============================================== 대댓글 ===============================================*/

    /*=============================== 각 게시판 공지글 ===================================*/
    private boolean subNotice;
    /*====================================================================================*/
}
