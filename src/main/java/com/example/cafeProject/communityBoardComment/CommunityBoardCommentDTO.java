package com.example.cafeProject.communityBoardComment;

import com.example.cafeProject.communityBoard.CommunityBoard;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Setter
@Getter
public class CommunityBoardCommentDTO {


    private int id;

    private String content;

    private Timestamp createDate;

    private int memberId;

    private int ref;

    private int step;

    private int level;

    private int communityBoardCommentId;


    private int communityBoardId;
}
