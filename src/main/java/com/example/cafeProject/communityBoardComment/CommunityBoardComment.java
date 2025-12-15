package com.example.cafeProject.communityBoardComment;

import com.example.cafeProject.communityBoard.CommunityBoard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CommentBoardComments")
public class CommunityBoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    private Timestamp createDate;
//    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "communityBoardId")
    private CommunityBoard communityBoard;
}
