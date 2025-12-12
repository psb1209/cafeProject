package com.example.cafeProject.noticeBoardComment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoardCommentDTO {
    private int id;
    private String content;
    private Timestamp createDate;
//    private int memberId;
    private int noticeBoardId;
}
