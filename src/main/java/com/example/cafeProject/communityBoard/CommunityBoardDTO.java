package com.example.cafeProject.communityBoard;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CommunityBoardDTO {

    private int id;

    private String subject;

    private String content;

    private int cnt;

    private Timestamp createDate;

    private int memberId;

    private int operationBoardCommentId;

    private String operationBoardCommentContent;
}
