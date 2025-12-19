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

    private int cnt;

    private Timestamp createDate;

    private int memberId;

    private int operationBoardCommentId;

    private String operationBoardCommentContent;
}
