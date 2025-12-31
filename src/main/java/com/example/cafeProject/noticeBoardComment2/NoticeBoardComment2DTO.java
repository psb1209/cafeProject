package com.example.cafeProject.noticeBoardComment2;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoardComment2DTO {

    private int id;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int noticeBoardId;

    private int noticeBoardCommentId;

    private int ref;
    private int step;
    private int level;





}
