package com.example.cafeProject._commentTest;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class PostCommentDTO {

    private int id;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int postId;

    private int postCommentId;

    private int ref;
    private int step;
    private int level;

    private boolean deleted = false;

    private Timestamp deletedAt;
}
