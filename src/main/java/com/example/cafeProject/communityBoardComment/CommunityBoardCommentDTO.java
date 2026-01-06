package com.example.cafeProject.communityBoardComment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CommunityBoardCommentDTO {

    private int id;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int communityBoardId;

    private int communityBoardCommentId;

    /*============================================== 대댓글 ===============================================*/
    private int ref;
    private int step;
    private int level;
    /*============================================== 대댓글 ===============================================*/





}
