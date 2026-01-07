package com.example.cafeProject.informationBoard;


import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class InformationBoardDTO {


    private int id;

    private String subject;

    private String content;

    private int cnt;

    private Timestamp createDate;

    private int memberId;

    /*============================================== 대댓글 ===============================================*/
    private int informationBoardCommentId;

    private String informationBoardCommentContent;
    /*============================================== 대댓글 ===============================================*/


    /*=============================== 각 게시판 공지글 ===================================*/
    private boolean subNotice;
    /*====================================================================================*/

}
