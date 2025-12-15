package com.example.cafeProject.noticeBoard;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoardDTO {
    private int id;
    private String subject;
    private String content;
    private int cnt;
    private Timestamp createDate;
    private int memberId;
}
