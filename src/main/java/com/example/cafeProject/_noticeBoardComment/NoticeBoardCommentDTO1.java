package com.example.cafeProject._noticeBoardComment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoardCommentDTO1 {
    private int id;
    private String content;
    private Timestamp createDate;
    private int memberId;
    private int noticeBoardId;
}
