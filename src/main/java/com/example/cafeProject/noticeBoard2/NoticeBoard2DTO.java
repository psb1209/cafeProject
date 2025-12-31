package com.example.cafeProject.noticeBoard2;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoard2DTO {

    private int id;

    private String subject;

    private String content;

    private int cnt;

    private Timestamp createDate;

    private int memberId;

    private int noticeBoardCommentId;

    private String noticeBoardBoardCommentContent;
}
