package com.example.cafeProject.operationBoardComment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class OperationBoardCommentDTO {

    private int id;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int operationBoardId;

    private int operationBoardCommentId;

}
