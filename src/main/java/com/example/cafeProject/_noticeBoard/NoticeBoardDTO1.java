package com.example.cafeProject._noticeBoard;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoticeBoardDTO1 {
    private int id;
    private String subject;
    private String content;
    private int cnt;
    private Timestamp createDate;
    private int memberId;

//    private int likeCnt;
    private int commentCnt;

}
